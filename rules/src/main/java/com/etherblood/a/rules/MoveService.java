package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.effects.targeting.TargetUtil;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import com.etherblood.a.rules.updates.SystemFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveService {

    private static final Logger LOG = LoggerFactory.getLogger(MoveService.class);

    private final GameSettings settings;
    private final EntityData data;
    private final HistoryRandom random;
    private final List<MoveReplay> history;
    private final CoreComponents core;
    private final GameEventListener eventListener;
    private final EffectiveStatsService effectiveStats;

    private final boolean backupsEnabled;
    private final boolean validateMoves;

    public MoveService(GameSettings settings, EntityData data, HistoryRandom random, GameEventListener eventListener) {
        this(settings, data, random, Collections.emptyList(), true, true, eventListener);
    }

    public MoveService(GameSettings settings, EntityData data, HistoryRandom random, List<MoveReplay> history, boolean backupsEnabled, boolean validateMoves, GameEventListener eventListener) {
        this.settings = settings;
        this.data = data;
        this.random = random;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.backupsEnabled = backupsEnabled;
        this.validateMoves = validateMoves;
        this.eventListener = eventListener;
        if (history != null) {
            this.history = new ArrayList<>(history);
        } else {
            this.history = null;
        }
        this.effectiveStats = new EffectiveStatsService(data);
    }

    public List<MoveReplay> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public List<Move> generate(boolean pruneFriendlyAttacks, boolean pruneSurrender) {
        List<Move> result = new ArrayList<>();
        for (int player : data.list(core.ACTIVE_PLAYER_PHASE)) {
            int phase = data.get(player, core.ACTIVE_PLAYER_PHASE);
            switch (phase) {
                case PlayerPhase.ATTACK: {
                    IntList minions = data.list(core.IN_BATTLE_ZONE);
                    for (int attacker : minions) {
                        if (!canDeclareAttack(player, attacker, false)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (pruneFriendlyAttacks) {
                                if (data.hasValue(target, core.OWNED_BY, player)) {
                                    continue;
                                }
                            }
                            if (canDeclareAttack(player, attacker, target, false)) {
                                result.add(new DeclareAttack(player, attacker, target));
                            }
                        }
                    }
                    IntList handCards = data.list(core.IN_HAND_ZONE);
                    for (int handCard : handCards) {
                        if (!canCast(player, handCard, false)) {
                            continue;
                        }
                        CardTemplate template = settings.templates.getCard(data.get(handCard, core.CARD_TEMPLATE));
                        CardCast cast = template.getAttackPhaseCast();
                        addCastMoves(player, handCard, cast, result);
                    }
                    result.add(new EndAttackPhase(player));
                    break;
                }
                case PlayerPhase.BLOCK: {
                    IntList minions = data.list(core.IN_BATTLE_ZONE);
                    for (int blocker : minions) {
                        if (!canDeclareBlock(player, blocker, false)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (canDeclareBlock(player, blocker, target, false)) {
                                result.add(new DeclareBlock(player, blocker, target));
                            }
                        }
                    }
                    IntList handCards = data.list(core.IN_HAND_ZONE);
                    for (int handCard : handCards) {
                        if (!canCast(player, handCard, false)) {
                            continue;
                        }
                        CardTemplate template = settings.templates.getCard(data.get(handCard, core.CARD_TEMPLATE));
                        CardCast cast = template.getBlockPhaseCast();
                        addCastMoves(player, handCard, cast, result);
                    }
                    result.add(new EndBlockPhase(player));
                    break;
                }
                case PlayerPhase.MULLIGAN: {
                    for (int card : data.list(core.IN_HAND_ZONE)) {
                        if (canDeclareMulligan(player, card, false)) {
                            result.add(new DeclareMulligan(player, card));
                        }
                    }
                    result.add(new EndMulliganPhase(player));
                    break;
                }
                default:
                    throw new AssertionError(phase);
            }
        }
        if (!pruneSurrender) {
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (canSurrender(player, false)) {
                    result.add(new Surrender(player));
                }
            }

        }
        return result;
    }

    private void addCastMoves(int player, int handCard, CardCast cast, List<Move> result) {
        if (cast.isTargeted()) {
            for (int target : TargetUtil.findValidTargets(data, handCard, cast.getTargets())) {
                result.add(new Cast(player, handCard, target));
            }
        } else {
            result.add(new Cast(player, handCard, ~0));
        }
    }

    public void apply(Move move) {
        Runnable runnable;
        if (move instanceof DeclareBlock) {
            DeclareBlock block = (DeclareBlock) move;
            runnable = () -> declareBlock(block.player, block.source, block.target);
        } else if (move instanceof Cast) {
            Cast cast = (Cast) move;
            runnable = () -> cast(cast.player, cast.source, cast.target);
        } else if (move instanceof DeclareAttack) {
            DeclareAttack declareAttack = (DeclareAttack) move;
            runnable = () -> declareAttack(declareAttack.player, declareAttack.source, declareAttack.target);
        } else if (move instanceof DeclareMulligan) {
            DeclareMulligan declareMulligan = (DeclareMulligan) move;
            runnable = () -> declareMulligan(declareMulligan.player, declareMulligan.card);
        } else if (move instanceof EndAttackPhase) {
            EndAttackPhase endAttackPhase = (EndAttackPhase) move;
            runnable = () -> endAttackPhase(endAttackPhase.player);
        } else if (move instanceof EndBlockPhase) {
            EndBlockPhase endBlockPhase = (EndBlockPhase) move;
            runnable = () -> endBlockPhase(endBlockPhase.player);
        } else if (move instanceof EndMulliganPhase) {
            EndMulliganPhase endMulliganPhase = (EndMulliganPhase) move;
            runnable = () -> endMulliganPhase(endMulliganPhase.player);
        } else if (move instanceof Surrender) {
            Surrender surrender = (Surrender) move;
            runnable = () -> surrender(surrender.player);
        } else if (move instanceof Start) {
            runnable = this::start;
        } else if (move instanceof Update) {
            runnable = this::update;
        } else {
            throw new AssertionError(move);
        }
        int randomSize = random.getHistory().size();
        runWithBackup(runnable::run);
        if (history != null) {
            int[] randomResults = random.getHistory().stream().skip(randomSize).toArray();
            MoveReplay replay = new MoveReplay();
            replay.move = move;
            replay.randomResults = randomResults;
            history.add(replay);
        }
    }

    private void start() {
        if (validateMoves) {
            canStart(true);
        }
        for (int player : data.list(core.PLAYER_INDEX)) {
            data.set(player, core.START_PHASE_REQUEST, PlayerPhase.MULLIGAN);
        }
        update();
    }

    private boolean canStart(boolean throwOnFail) {
        if (!data.list(core.ACTIVE_PLAYER_PHASE).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, there are already active players.");
            }
            return false;
        }
        if (!data.list(core.PLAYER_RESULT).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, at least one player already won or lost.");
            }
            return false;
        }
        return true;
    }

    private void endAttackPhase(int player) {
        if (validateMoves) {
            canEndAttackPhase(player, true);
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.ATTACK);
        update();
    }

    private boolean canEndAttackPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end attack phase, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        return true;
    }

    private void endBlockPhase(int player) {
        if (validateMoves) {
            canEndBlockPhase(player, true);
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.BLOCK);
        update();
    }

    private boolean canEndBlockPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " is not in block phase.");
            }
            return false;
        }
        return true;
    }

    private void endMulliganPhase(int player) {
        if (validateMoves) {
            canEndMulliganPhase(player, true);
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.MULLIGAN);
        update();
    }

    private boolean canEndMulliganPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end mulligan phase, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        return true;
    }

    private void declareAttack(int player, int attacker, int target) {
        if (validateMoves) {
            canDeclareAttack(player, attacker, target, true);
        }
        data.set(attacker, core.ATTACKS_TARGET, target);
        update();
    }

    private boolean canDeclareAttack(int player, int attacker, int target, boolean throwOnFail) {
        if (!data.has(target, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + target + " is not in battle zone.");
            }
            return false;
        }
        return canDeclareAttack(player, attacker, throwOnFail);
    }

    private boolean canDeclareAttack(int player, int attacker, boolean throwOnFail) {
        if (!data.hasValue(attacker, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " does not own attacker #" + attacker + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        if (!data.has(attacker, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is not in battle zone.");
            }
            return false;
        }
        if (data.has(attacker, core.CANNOT_ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " can not attack.");
            }
            return false;
        }
        if (data.has(attacker, core.SUMMONING_SICKNESS) && !effectiveStats.isFastToAttack(attacker)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " has summoning sickness.");
            }
            return false;
        }
        if (data.has(attacker, core.TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is tired.");
            }
            return false;
        }
        if (data.has(attacker, core.ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is already attacking.");
            }
            return false;
        }
        return true;
    }

    private void declareBlock(int player, int blocker, int attacker) {
        if (validateMoves) {
            canDeclareBlock(player, blocker, attacker, true);
        }
        data.set(blocker, core.BLOCKS_ATTACKER, attacker);
        update();
    }

    private boolean canDeclareBlock(int player, int blocker, int attacker, boolean throwOnFail) {
        if (!data.has(attacker, core.ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, attacker #" + attacker + " is not attacking.");
            }
            return false;
        }
        if (data.has(attacker, core.CANNOT_BE_BLOCKED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, attacker #" + attacker + " can not be blocked.");
            }
            return false;
        }
        return canDeclareBlock(player, blocker, throwOnFail);
    }

    private boolean canDeclareBlock(int player, int blocker, boolean throwOnFail) {
        if (!data.hasValue(blocker, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " does not own blocker #" + blocker + ".");
            }
            return false;
        }
        if (!data.has(blocker, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is not in battle zone.");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " is not in block phase.");
            }
            return false;
        }
        if (data.has(blocker, core.SUMMONING_SICKNESS) && !effectiveStats.isFastToDefend(blocker)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " has summoning sickness.");
            }
            return false;
        }
        if (data.has(blocker, core.TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is tired.");
            }
            return false;
        }
        if (data.has(blocker, core.BLOCKS_ATTACKER)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is already blocking.");
            }
            return false;
        }
        if (data.has(blocker, core.CANNOT_BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " can not block.");
            }
            return false;
        }
        boolean allAttackersUnblockable = true;
        for (int attacker : data.list(core.ATTACKS_TARGET)) {
            int target = data.get(attacker, core.ATTACKS_TARGET);
            if (target == blocker) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is being attacked.");
                }
                return false;
            }
            if (allAttackersUnblockable && !data.has(attacker, core.CANNOT_BE_BLOCKED)) {
                allAttackersUnblockable = false;
            }
        }
        if (allAttackersUnblockable) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, nobody is attacking.");
            }
            return false;
        }
        return true;
    }

    private void cast(int player, int castable, Integer target) {
        if (validateMoves) {
            canCast(player, castable, target != null ? target : ~0, true);
        }
        String cardName = getCardName(castable);
        try {
            data.set(castable, core.CAST_TARGET, target != null ? target : ~0);
            update();
        } catch (Throwable t) {
            LOG.error("Error when casting {}.", cardName);
            throw t;
        }
    }

    private String getCardName(int entity) {
        OptionalInt templateId = data.getOptional(entity, core.CARD_TEMPLATE);
        if (templateId.isPresent()) {
            return settings.templates.getCard(templateId.getAsInt()).getTemplateName();
        }
        return null;
    }

    private boolean canCast(int player, int castable, boolean throwOnFail) {
        if (!data.hasValue(castable, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " does not own castable #" + castable + ".");
            }
            return false;
        }
        if (!data.has(castable, core.IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + " is not in hand zone.");
            }
            return false;
        }
        CardTemplate template = settings.templates.getCard(data.get(castable, core.CARD_TEMPLATE));
        CardCast cast;
        OptionalInt phase = data.getOptional(player, core.ACTIVE_PLAYER_PHASE);
        if (phase.isPresent()) {
            switch (phase.getAsInt()) {
                case PlayerPhase.ATTACK:
                    cast = template.getAttackPhaseCast();
                    if (cast == null) {
                        if (throwOnFail) {
                            throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in attack phase.");
                        }
                        return false;
                    }
                    break;
                case PlayerPhase.BLOCK:
                    cast = template.getBlockPhaseCast();
                    if (cast == null) {
                        if (throwOnFail) {
                            throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in block phase.");
                        }
                        return false;
                    }
                    break;
                default:
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, casting is not possible during current phase.");
                    }
                    return false;
            }
        } else {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " is not the active player.");
            }
            return false;
        }
        if (cast.getManaCost() > data.getOptional(player, core.MANA).orElse(0)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + ", player #" + player + " does not have enough mana.");
            }
            return false;
        }
        return true;
    }

    private boolean canCast(int player, int castable, int target, boolean throwOnFail) {
        CardTemplate template = settings.templates.getCard(data.get(castable, core.CARD_TEMPLATE));
        OptionalInt phase = data.getOptional(player, core.ACTIVE_PLAYER_PHASE);
        if (phase.isPresent()) {
            CardCast cast;
            if (phase.getAsInt() == PlayerPhase.ATTACK) {
                cast = template.getAttackPhaseCast();
                if (cast != null && cast.isTargeted() && !TargetUtil.isValidTarget(data, castable, target, cast.getTargets())) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not valid.");
                    }
                    return false;
                }
            } else if (phase.getAsInt() == PlayerPhase.BLOCK) {
                cast = template.getBlockPhaseCast();
                if (cast != null && cast.isTargeted() && !TargetUtil.isValidTarget(data, castable, target, cast.getTargets())) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not valid.");
                    }
                    return false;
                }
            }
        }
        return canCast(player, castable, throwOnFail);
    }

    private void declareMulligan(int player, int card) {
        if (validateMoves) {
            canDeclareMulligan(player, card, true);
        }
        data.set(card, core.MULLIGAN, card);
        update();
    }

    private boolean canDeclareMulligan(int player, int card, boolean throwOnFail) {
        if (!data.hasValue(card, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " does not own card #" + card + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        if (!data.has(card, core.IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, card #" + card + " is not in hand zone.");
            }
            return false;
        }
        if (data.has(card, core.MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, card #" + card + " is already mulliganed.");
            }
            return false;
        }
        return true;
    }

    private void surrender(int player) {
        if (validateMoves) {
            canSurrender(player, true);
        }
        data.set(player, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
        update();
    }

    private boolean canSurrender(int player, boolean throwOnFail) {
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to surrender, player #" + player + " already lost.");
            }
            return false;
        }
        if (hasPlayerWon(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to surrender, player #" + player + " already won.");
            }
            return false;
        }
        return true;
    }

    private void runWithBackup(Runnable runnable) {
        if (!backupsEnabled) {
            runnable.run();
            return;
        }
        int randomHistorySize = random.getHistory().size();
        EntityData backup = new SimpleEntityData(data.getComponents());
        EntityUtil.copy(data, backup);
        LOG.debug("Created backup.");
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
        new SystemFactory(data, settings.templates, random, eventListener).build().run();
//        IntList requests = new IntList();
//        requests.add(core.DAMAGE_REQUEST);
//        requests.add(core.DEATH_REQUEST);
//        requests.add(core.END_PHASE_REQUEST);
//        requests.add(core.START_PHASE_REQUEST);
//        requests.add(core.PLAYER_RESULT_REQUEST);
//        requests.add(core.CAST_TARGET);
//        BooleanSupplier requestExists = () -> requests.stream().flatMap(component -> data.list(component).stream()).findAny().isPresent();
//        while (requestExists.getAsBoolean()) {
//            eventListener.nextIteration();
//            runSystems(systems);
//        }
    }

    private void runSystems(List<AbstractSystem> systems) {
        for (AbstractSystem system : systems) {
            system.run(settings, data, random, eventListener);
        }
        assert validateStateLegal();
    }

    private boolean validateStateLegal() {
        IntList playerResults = data.list(core.PLAYER_RESULT);
        IntList winners = new IntList();
        IntList losers = new IntList();
        for (int player : playerResults) {
            if (data.get(player, core.PLAYER_RESULT) == PlayerResult.WIN) {
                winners.add(player);
            } else {
                losers.add(player);
            }
        }
        IntList players = data.list(core.PLAYER_INDEX);
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

        if (data.list(core.ACTIVE_PLAYER_PHASE).isEmpty() && data.list(core.START_PHASE_REQUEST).isEmpty()) {
            if (winners.size() + losers.size() != players.size()) {
                throw new IllegalStateException("Some players are still without playerResult and there is no activePlayerPhase.");
            }
        }

        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (!data.has(minion, core.OWNED_BY)) {
                throw new IllegalStateException("Minion without owner in battle zone.");
            }
            if (!data.has(minion, core.MINION_TEMPLATE)) {
                throw new IllegalStateException("Minion without template in battle zone.");
            }
        }

        for (int minion : data.list(core.IN_HAND_ZONE)) {
            if (!data.has(minion, core.OWNED_BY)) {
                throw new IllegalStateException("Card without owner in hand zone.");
            }
            if (!data.has(minion, core.CARD_TEMPLATE)) {
                throw new IllegalStateException("Card without template in hand zone.");
            }
        }

        for (int minion : data.list(core.IN_LIBRARY_ZONE)) {
            if (!data.has(minion, core.OWNED_BY)) {
                throw new IllegalStateException("Card without owner in library zone.");
            }
            if (!data.has(minion, core.CARD_TEMPLATE)) {
                throw new IllegalStateException("Card without template in library zone.");
            }
        }

        for (int minion : data.list(core.ATTACKS_TARGET)) {
            if (!data.has(minion, core.IN_BATTLE_ZONE)) {
                throw new IllegalStateException("Attacking minion is not in battle zone.");
            }
        }

        return true;
    }

    private boolean hasPlayerWon(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.WIN);
    }

    private boolean hasPlayerLost(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS);
    }

    public HistoryRandom getRandom() {
        return random;
    }
}
