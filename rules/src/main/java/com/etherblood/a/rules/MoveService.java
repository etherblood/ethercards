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
import com.etherblood.a.rules.targeting.TargetUtil;
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
    private final MoveAvailabilityService moveAvailability;

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
        this.moveAvailability = new MoveAvailabilityService(data, settings.templates);
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
                case PlayerPhase.ATTACK: {
                    IntList minions = data.list(core.IN_BATTLE_ZONE);
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
                    IntList handCards = data.list(core.IN_HAND_ZONE);
                    for (int handCard : handCards) {
                        if (!moveAvailability.canCast(player, handCard, false)) {
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
                        if (!moveAvailability.canDeclareBlock(player, blocker, false)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (moveAvailability.canDeclareBlock(player, blocker, target, false)) {
                                result.add(new DeclareBlock(player, blocker, target));
                            }
                        }
                    }
                    IntList handCards = data.list(core.IN_HAND_ZONE);
                    for (int handCard : handCards) {
                        if (!moveAvailability.canCast(player, handCard, false)) {
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
                        if (moveAvailability.canDeclareMulligan(player, card, false)) {
                            result.add(new DeclareMulligan(player, card));
                        }
                    }
                    result.add(new EndMulliganPhase(player));
                    break;
                }
                default:
                    throw new AssertionError(phase);
            }
        });
        if (!pruneSurrender) {
            if (moveAvailability.canSurrender(player, false)) {
                result.add(new Surrender(player));
            }

        }
        return result;
    }

    private void addCastMoves(int player, int handCard, CardCast cast, List<Move> result) {
        if (cast.isTargeted()) {
            for (int target : TargetUtil.findValidTargets(data, handCard, cast.getTargets())) {
                if (moveAvailability.canCast(player, handCard, target, false)) {
                    result.add(new Cast(player, handCard, target));
                }
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
            moveAvailability.canStart(true);
        }
        for (int player : data.list(core.PLAYER_INDEX)) {
            data.set(player, core.START_PHASE_REQUEST, PlayerPhase.MULLIGAN);
        }
        update();
    }

    private void endAttackPhase(int player) {
        if (validateMoves) {
            moveAvailability.canEndAttackPhase(player, true);
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.ATTACK);
        update();
    }

    private void endBlockPhase(int player) {
        if (validateMoves) {
            moveAvailability.canEndBlockPhase(player, true);
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.BLOCK);
        update();
    }

    private void endMulliganPhase(int player) {
        if (validateMoves) {
            moveAvailability.canEndMulliganPhase(player, true);
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.MULLIGAN);
        update();
    }

    private void declareAttack(int player, int attacker, int target) {
        if (validateMoves) {
            moveAvailability.canDeclareAttack(player, attacker, target, true);
        }
        data.set(attacker, core.ATTACKS_TARGET, target);
        update();
    }

    private void declareBlock(int player, int blocker, int attacker) {
        if (validateMoves) {
            moveAvailability.canDeclareBlock(player, blocker, attacker, true);
        }
        data.set(blocker, core.BLOCKS_ATTACKER, attacker);
        update();
    }

    private void cast(int player, int castable, Integer target) {
        if (validateMoves) {
            moveAvailability.canCast(player, castable, target != null ? target : ~0, true);
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
        data.set(player, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
        update();
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
            if (!data.has(minion, core.CARD_TEMPLATE)) {
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

    public HistoryRandom getRandom() {
        return random;
    }
}
