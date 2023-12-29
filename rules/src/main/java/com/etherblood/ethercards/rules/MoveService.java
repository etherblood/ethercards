package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.entities.ComponentMeta;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.entities.SimpleEntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.rules.moves.DeclareAttack;
import com.etherblood.ethercards.rules.moves.DeclareBlock;
import com.etherblood.ethercards.rules.moves.DeclareMulligan;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.rules.moves.EndMulliganPhase;
import com.etherblood.ethercards.rules.moves.Move;
import com.etherblood.ethercards.rules.moves.Start;
import com.etherblood.ethercards.rules.moves.Surrender;
import com.etherblood.ethercards.rules.moves.Update;
import com.etherblood.ethercards.rules.moves.UseAbility;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.StatModifier;
import com.etherblood.ethercards.rules.templates.TargetSelection;
import com.etherblood.ethercards.rules.templates.ZoneState;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import com.etherblood.ethercards.rules.updates.TriggerService;
import com.etherblood.ethercards.rules.updates.ZoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public class MoveService {

    private static final Logger LOG = LoggerFactory.getLogger(MoveService.class);

    private final EntityData data;
    private final GameTemplates templates;
    private final HistoryRandom random;
    private final List<MoveReplay> history;
    private final CoreComponents core;
    private final GameEventListener eventListener;
    private final MoveAvailabilityService moveAvailability;
    private final Runnable updateResolver;

    private final boolean backupsEnabled;
    private final boolean validateMoves;

    public MoveService(EntityData data, GameTemplates templates, HistoryRandom random, GameEventListener eventListener, Runnable updateResolver) {
        this(data, templates, random, Collections.emptyList(), true, true, eventListener, updateResolver);
    }

    public MoveService(EntityData data, GameTemplates templates, HistoryRandom random, List<MoveReplay> history, boolean backupsEnabled, boolean validateMoves, GameEventListener eventListener, Runnable updateResolver) {
        this.data = data;
        this.templates = templates;
        this.random = random;
        this.core = data.getSchema().getModule(CoreComponents.class);
        this.backupsEnabled = backupsEnabled;
        this.validateMoves = validateMoves;
        this.eventListener = eventListener;
        if (history != null) {
            this.history = new ArrayList<>(history);
        } else {
            this.history = null;
        }
        this.moveAvailability = new MoveAvailabilityService(data, templates);
        this.updateResolver = updateResolver;
    }

    public List<MoveReplay> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public List<Move> generate(boolean pruneSurrender) {
        List<Move> result = new ArrayList<>();
        for (int player : data.list(core.PLAYER_INDEX)) {
            result.addAll(generate(pruneSurrender, player));
        }
        return result;
    }

    public List<Move> generate(boolean pruneSurrender, int player) {
        List<Move> result = new ArrayList<>();
        data.getOptional(player, core.ACTIVE_PLAYER_PHASE).ifPresent(phase -> {
            switch (phase) {
                case PlayerPhase.ATTACK -> {
                    EntityList minions = data.list(core.IN_BATTLE_ZONE);
                    for (int attacker : minions) {
                        if (!moveAvailability.canDeclareAttack(player, attacker, false)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (moveAvailability.canDeclareAttack(player, attacker, target, false)) {
                                result.add(new DeclareAttack(player, attacker, target));
                            }
                        }
                    }
                    addAllCastMoves(player, result);
                    addAllUseAbilityMoves(player, result);
                    result.add(new EndAttackPhase(player));
                }
                case PlayerPhase.BLOCK -> {
                    EntityList minions = data.list(core.IN_BATTLE_ZONE);
                    for (int blocker : minions) {
                        if (!moveAvailability.canDeclareBlock(player, blocker, false)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (moveAvailability.canDeclareBlock(player, blocker, target, false)) {
                                result.add(new DeclareBlock(player, blocker, target));
                            }
                        }
                    }
                    addAllCastMoves(player, result);
                    addAllUseAbilityMoves(player, result);
                    result.add(new EndBlockPhase(player));
                }
                case PlayerPhase.MULLIGAN -> {
                    for (int card : data.list(core.IN_HAND_ZONE)) {
                        if (moveAvailability.canDeclareMulligan(player, card, false)) {
                            result.add(new DeclareMulligan(player, card));
                        }
                    }
                    result.add(new EndMulliganPhase(player));
                }
                default -> throw new AssertionError(phase);
            }
        });
        if (!pruneSurrender) {
            if (moveAvailability.canSurrender(player, false)) {
                result.add(new Surrender(player));
            }

        }
        return result;
    }

    private void addAllCastMoves(int player, List<Move> result) {
        for (int handCard : data.list(core.IN_HAND_ZONE)) {
            if (!moveAvailability.canCast(player, handCard, false)) {
                continue;
            }
            CardTemplate template = templates.getCard(data.get(handCard, core.CARD_TEMPLATE));
            addCastMoves(player, handCard, template.getHand().getCast().getTarget(), result);
        }
    }

    private void addCastMoves(int player, int handCard, TargetSelection targeting, List<Move> result) {
        EntityList targets = targeting.getValidTargets(data, templates, handCard);
        if (targets.isEmpty()) {
            if (!targeting.requiresTarget()) {
                if (moveAvailability.canCast(player, handCard, null, false)) {
                    result.add(new Cast(player, handCard, null));
                }
            }
        } else {
            for (int target : targets) {
                if (moveAvailability.canCast(player, handCard, target, false)) {
                    result.add(new Cast(player, handCard, target));
                }
            }
        }
    }

    private void addAllUseAbilityMoves(int player, List<Move> result) {
        for (int entity : data.list(core.ACTIVATED_ABILITY)) {
            if (!moveAvailability.canUseAbility(player, entity, false)) {
                continue;
            }
            CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
            ZoneState zone = template.getActiveZone(entity, data);
            addUseAbilityMoves(player, entity, zone.getActivated().getTarget(), result);
        }
    }

    private void addUseAbilityMoves(int player, int entity, TargetSelection targeting, List<Move> result) {
        EntityList targets = targeting.getValidTargets(data, templates, entity);
        if (targets.isEmpty()) {
            if (!targeting.requiresTarget()) {
                if (moveAvailability.canUseAbility(player, entity, null, false)) {
                    result.add(new UseAbility(player, entity));
                }
            }
        } else {
            for (int target : targets) {
                if (moveAvailability.canUseAbility(player, entity, target, false)) {
                    result.add(new UseAbility(player, entity, target));
                }
            }
        }
    }

    public void apply(Move move) {
        Runnable runnable;
        if (move instanceof DeclareBlock block) {
            runnable = () -> declareBlock(block.player(), block.source(), block.target());
        } else if (move instanceof Cast cast) {
            runnable = () -> cast(cast.player(), cast.source(), cast.target());
        } else if (move instanceof DeclareAttack declareAttack) {
            runnable = () -> declareAttack(declareAttack.player(), declareAttack.source(), declareAttack.target());
        } else if (move instanceof UseAbility useAbility) {
            runnable = () -> useAbility(useAbility.player(), useAbility.source(), useAbility.target());
        } else if (move instanceof DeclareMulligan declareMulligan) {
            runnable = () -> declareMulligan(declareMulligan.player(), declareMulligan.card());
        } else if (move instanceof EndAttackPhase endAttackPhase) {
            runnable = () -> endAttackPhase(endAttackPhase.player());
        } else if (move instanceof EndBlockPhase endBlockPhase) {
            runnable = () -> endBlockPhase(endBlockPhase.player());
        } else if (move instanceof EndMulliganPhase endMulliganPhase) {
            runnable = () -> endMulliganPhase(endMulliganPhase.player());
        } else if (move instanceof Surrender surrender) {
            runnable = () -> surrender(surrender.player());
        } else if (move instanceof Start) {
            runnable = this::start;
        } else if (move instanceof Update) {
            runnable = this::update;
        } else {
            throw new AssertionError(move);
        }
        int randomSize = random.getHistory().size();
        runWithBackup(runnable);
        if (history != null) {
            int[] randomResults = random.getHistory().stream().skip(randomSize).toArray();
            MoveReplay replay = new MoveReplay(move, randomResults);
            history.add(replay);
        }
    }

    private void start() {
        if (validateMoves) {
            moveAvailability.canStart(true);
        }

        Map<Integer, IntList> playerLibraries = new HashMap<>();
        for (int card : data.list(core.IN_LIBRARY_ZONE)) {
            int owner = data.get(card, core.OWNER);
            playerLibraries.computeIfAbsent(owner, x -> new IntList()).add(card);
        }
        for (int player : data.list(core.INITIAL_DRAWS)) {
            IntList playerLibrary = playerLibraries.computeIfAbsent(player, x -> new IntList());
            int draws = data.get(player, core.INITIAL_DRAWS);
            for (int i = 0; i < draws && !playerLibrary.isEmpty(); i++) {
                int card = playerLibrary.swapRemoveAt(random.applyAsInt(playerLibrary.size()));
                data.remove(card, core.IN_LIBRARY_ZONE);
                data.set(card, core.IN_HAND_ZONE, 1);
            }
        }
        data.clear(core.INITIAL_DRAWS);

        ZoneService zoneService = new ZoneService(data, templates, random, eventListener);
        TriggerService triggerService = new TriggerService(data, templates, random, eventListener);
        for (int card : data.list(core.CARD_TEMPLATE)) {
            zoneService.initComponents(card);
            triggerService.initEffects(card);
        }

        for (int player : data.list(core.TEAM_INDEX)) {
            data.set(player, core.ACTIVE_TEAM_PHASE, PlayerPhase.MULLIGAN);
        }
        for (int player : data.list(core.PLAYER_INDEX)) {
            data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN);
        }
        update();
    }

    private void endAttackPhase(int player) {
        if (validateMoves) {
            moveAvailability.canEndAttackPhase(player, true);
        }
        data.remove(player, core.ACTIVE_PLAYER_PHASE);
        update();
    }

    private void endBlockPhase(int player) {
        if (validateMoves) {
            moveAvailability.canEndBlockPhase(player, true);
        }
        data.remove(player, core.ACTIVE_PLAYER_PHASE);
        update();
    }

    private void endMulliganPhase(int player) {
        if (validateMoves) {
            moveAvailability.canEndMulliganPhase(player, true);
        }
        data.remove(player, core.ACTIVE_PLAYER_PHASE);
        update();
    }

    private void declareAttack(int player, int attacker, int target) {
        if (validateMoves) {
            moveAvailability.canDeclareAttack(player, attacker, target, true);
        }
        data.set(attacker, core.ATTACK_TARGET, target);
        update();
    }

    private void useAbility(int player, int source, Integer target) {
        if (validateMoves) {
            moveAvailability.canUseAbility(player, source, target, true);
        }
        data.set(source, core.USE_ABILITY_TARGET, Objects.requireNonNullElse(target, ~0));
        update();
    }

    private void declareBlock(int player, int blocker, int attacker) {
        if (validateMoves) {
            moveAvailability.canDeclareBlock(player, blocker, attacker, true);
        }
        data.set(blocker, core.BLOCK_TARGET, attacker);
        update();
    }

    private void cast(int player, int castable, Integer target) {
        if (validateMoves) {
            moveAvailability.canCast(player, castable, target, true);
        }
        String cardName = getCardName(castable);
        try {
            data.set(castable, core.CAST_TARGET, Objects.requireNonNullElse(target, ~0));
            update();
        } catch (Throwable t) {
            LOG.error("Error when casting {}.", cardName);
            throw t;
        }
    }

    private String getCardName(int entity) {
        OptionalInt templateId = data.getOptional(entity, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            return templates.getCard(templateId.getAsInt()).getTemplateName();
        }
        return null;
    }

    private void declareMulligan(int player, int card) {
        if (validateMoves) {
            moveAvailability.canDeclareMulligan(player, card, true);
        }
        data.set(card, core.MULLIGAN, card);
        update();
    }

    private void surrender(int player) {
        if (validateMoves) {
            moveAvailability.canSurrender(player, true);
        }
        SystemsUtil.setPlayerResult(data, player, PlayerResult.LOSS);
        update();
    }

    private void runWithBackup(Runnable runnable) {
        if (!backupsEnabled) {
            runnable.run();
            return;
        }
        int randomHistorySize = random.getHistory().size();
        EntityData backup = new SimpleEntityData(data.getSchema());
        EntityUtil.copy(data, backup);
        LOG.trace("Created backup.");
        try {
            runnable.run();
        } catch (Throwable t) {
            while (random.getHistory().size() > randomHistorySize) {
                random.getHistory().removeLast();
            }
            EntityUtil.copy(backup, data);
            LOG.warn("Rolled back due to exception.");
            throw t;
        }
    }

    private void update() {
        updateResolver.run();
        assert validateStateLegal();
    }

    private boolean validateStateLegal() {
        try {
            for (int player : data.list(core.TEAM)) {
                int team = data.get(player, core.TEAM);
                if (!data.has(team, core.TEAM_INDEX)) {
                    throw new IllegalStateException("Player has team without index.");
                }
            }

            for (int card : data.list(core.OWNER)) {
                int player = data.get(card, core.OWNER);
                if (!data.has(player, core.PLAYER_INDEX)) {
                    throw new IllegalStateException("Card has owner without index.");
                }
                int team = data.get(player, core.TEAM);
                if (!data.hasValue(card, core.TEAM, team)) {
                    throw new IllegalStateException("Card has different team than its owner.");
                }
            }

            EntityList playerResults = data.list(core.PLAYER_RESULT);
            IntList winners = new IntList();
            IntList losers = new IntList();
            for (int player : playerResults) {
                if (data.get(player, core.PLAYER_RESULT) == PlayerResult.WIN) {
                    winners.add(player);
                } else {
                    losers.add(player);
                }
            }
            EntityList players = data.list(core.PLAYER_INDEX);
            if (!winners.isEmpty()) {
                for (int player : players) {
                    if (data.has(player, core.PLAYER_RESULT)) {
                        continue;
                    }
                    throw new IllegalStateException("Players without playerResult exist when there is already a winner.");
                }
            }
            for (int player : data.list(core.ACTIVE_PLAYER_PHASE)) {
                if (data.has(player, core.PLAYER_RESULT)) {
                    throw new IllegalStateException("Active player has a playerResult.");
                }
                if (!data.has(player, core.PLAYER_INDEX)) {
                    throw new IllegalStateException("Active player does not have a playerIndex");
                }
            }

            if (data.list(core.ACTIVE_TEAM_PHASE).isEmpty()) {
                if (data.list(core.ACTIVE_PLAYER_PHASE).nonEmpty()) {
                    throw new IllegalStateException("Some players are still active when there is no activeTeamPhase.");
                }
                if (winners.size() + losers.size() != players.size()) {
                    throw new IllegalStateException("Some players are still without playerResult and there is no activePlayerPhase.");
                }
            }

            for (int minion : data.list(core.IN_BATTLE_ZONE)) {
                if (!data.has(minion, core.OWNER)) {
                    throw new IllegalStateException("Minion without owner in battle zone.");
                }
                if (!data.has(minion, core.CARD_TEMPLATE)) {
                    throw new IllegalStateException("Minion without template in battle zone.");
                }
            }

            for (int minion : data.list(core.IN_HAND_ZONE)) {
                if (!data.has(minion, core.OWNER)) {
                    throw new IllegalStateException("Card without owner in hand zone.");
                }
                if (!data.has(minion, core.CARD_TEMPLATE)) {
                    throw new IllegalStateException("Card without template in hand zone.");
                }
            }

            for (int minion : data.list(core.IN_LIBRARY_ZONE)) {
                if (!data.has(minion, core.OWNER)) {
                    throw new IllegalStateException("Card without owner in library zone.");
                }
                if (!data.has(minion, core.CARD_TEMPLATE)) {
                    throw new IllegalStateException("Card without template in library zone.");
                }
            }

            for (int minion : data.list(core.IN_GRAVEYARD_ZONE)) {
                if (!data.has(minion, core.OWNER)) {
                    throw new IllegalStateException("Card without owner in library zone.");
                }
                if (!data.has(minion, core.CARD_TEMPLATE)) {
                    throw new IllegalStateException("Card without template in library zone.");
                }
            }

            for (int minion : data.list(core.ATTACK_TARGET)) {
                if (!data.has(minion, core.IN_BATTLE_ZONE)) {
                    throw new IllegalStateException("Attacking minion is not in battle zone.");
                }
            }

            for (int entity : data.list(core.OWNER)) {
                int owner = data.get(entity, core.OWNER);
                int team = data.get(entity, core.TEAM);
                if (!data.hasValue(owner, core.TEAM, team)) {
                    throw new IllegalStateException("Entities owner has a different team than entity.");
                }
            }

            for (int card : data.list(core.CARD_TEMPLATE)) {
                int templateId = data.get(card, core.CARD_TEMPLATE);
                CardTemplate template = templates.getCard(templateId);

                Map<Integer, List<Effect>> triggers = template.getActiveZone(card, data).getPassive();
                for (int triggerComponent : triggers.keySet()) {
                    if (!data.has(card, triggerComponent)) {
                        throw new IllegalStateException("Card has unmapped " + data.getSchema().getMeta(triggerComponent).name + " trigger component.");
                    }
                }

                // commented out because it is too slow
//                for (ComponentMeta meta : data.getComponents().getMetas()) {
//                    if (meta.name.startsWith("TRIGGER_")) {
//                        if (data.has(card, meta.id) && !triggers.containsKey(meta.id)) {
//                            throw new IllegalStateException("Card has mapped " + meta.name + " without actually having this trigger active.");
//                        }
//                    }
//                }
                ZoneState activeZone = template.getActiveZone(card, data);
                for (Map.Entry<Integer, StatModifier> entry : activeZone.getStatModifiers().entrySet()) {
                    ComponentMeta meta = data.getSchema().getMeta(entry.getKey());
                    if (meta.name.endsWith("_AURA") && !data.has(card, meta.id)) {
                        throw new IllegalStateException("Entity with template name " + template.getTemplateName() + " has " + meta.name + " modifier without matching component.");
                    }
                }
            }

            return true;
        } catch (IllegalStateException e) {
            LOG.error("{}", EntityUtil.toMap(data));
            throw e;
        }
    }

    public HistoryRandom getRandom() {
        return random;
    }

    public GameEventListener getEvents() {
        return eventListener;
    }
}
