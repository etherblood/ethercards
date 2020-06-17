package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.rules.systems.BlockSystem;
import com.etherblood.a.rules.systems.CastSystem;
import com.etherblood.a.rules.systems.DamageSystem;
import com.etherblood.a.rules.systems.DeathSystem;
import com.etherblood.a.rules.systems.DrawSystem;
import com.etherblood.a.rules.systems.EndAttackPhaseSystem;
import com.etherblood.a.rules.systems.EndBlockPhaseBattleSystem;
import com.etherblood.a.rules.systems.EndMulliganPhaseSystem;
import com.etherblood.a.rules.systems.OnDeathSystem;
import com.etherblood.a.rules.systems.OnSurvivalSystem;
import com.etherblood.a.rules.systems.PlayerStatusSystem;
import com.etherblood.a.rules.systems.TemporariesCleanupSystem;
import com.etherblood.a.rules.systems.UpkeepSystem;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.effects.targeting.TargetUtil;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final boolean backupsEnabled;
    private final boolean validateMoves;

    private final List<AbstractSystem> endAttackPhaseSystems;
    private final List<AbstractSystem> endBlockPhaseSystems;
    private final List<AbstractSystem> endMulliganPhaseSystems;
    private final List<AbstractSystem> blockSystems;
    private final List<AbstractSystem> castSystems;
    private final List<AbstractSystem> surrenderSystems;
    private final List<AbstractSystem> startSystems;

    public MoveService(GameSettings settings, EntityData data, HistoryRandom random) {
        this(settings, data, random, Collections.emptyList(), true, true);
    }

    public MoveService(GameSettings settings, EntityData data, HistoryRandom random, List<MoveReplay> history, boolean backupsEnabled, boolean validateMoves) {
        this.settings = settings;
        this.data = data;
        this.random = random;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.backupsEnabled = backupsEnabled;
        this.validateMoves = validateMoves;
        List<AbstractSystem> generalSystems = Arrays.asList(
                new DrawSystem(),
                new DamageSystem(),
                new OnDeathSystem(),
                new OnSurvivalSystem(),
                new DeathSystem(),
                new PlayerStatusSystem(),
                new TemporariesCleanupSystem()
        );

        endBlockPhaseSystems = new ArrayList<>();
        endBlockPhaseSystems.add(new EndBlockPhaseBattleSystem());
        endBlockPhaseSystems.addAll(generalSystems);
        endBlockPhaseSystems.add(new UpkeepSystem());
        endBlockPhaseSystems.addAll(generalSystems);

        endAttackPhaseSystems = new ArrayList<>();
        endAttackPhaseSystems.add(new EndAttackPhaseSystem());
        endAttackPhaseSystems.addAll(generalSystems);

        endMulliganPhaseSystems = new ArrayList<>();
        endMulliganPhaseSystems.add(new EndMulliganPhaseSystem());
        endMulliganPhaseSystems.addAll(generalSystems);
        endMulliganPhaseSystems.add(new UpkeepSystem());
        endMulliganPhaseSystems.addAll(generalSystems);

        blockSystems = new ArrayList<>();
        blockSystems.add(new BlockSystem());
        blockSystems.addAll(generalSystems);

        castSystems = new ArrayList<>();
        castSystems.add(new CastSystem(settings.templates));
        castSystems.addAll(generalSystems);

        surrenderSystems = new ArrayList<>();
        surrenderSystems.addAll(generalSystems);

        startSystems = new ArrayList<>();
        startSystems.addAll(generalSystems);

        if (history != null) {
            this.history = new ArrayList<>(history);
        } else {
            this.history = null;
        }
    }

    public List<MoveReplay> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void move(Move move) {
        Runnable runnable;
        if (move instanceof Block) {
            Block block = (Block) move;
            runnable = () -> block(block.player, block.source, block.target);
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
        } else {
            throw new AssertionError(move);
        }
        int randomSize = random.getHistory().size();
        runWithBackup(() -> {
            runnable.run();
        });
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
            verifyCanStart(true);
        }
        for (int player : data.list(core.PLAYER_INDEX)) {
            data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE);
        }
        runSystems(startSystems);
    }

    public boolean canStart() {
        return verifyCanStart(false);
    }

    private boolean verifyCanStart(boolean throwOnFail) {
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
            verifyCanEndAttackPhase(player, true);
        }
        data.set(player, core.END_PHASE, 1);
        runSystems(endAttackPhaseSystems);
    }

    public boolean canEndAttackPhase(int player) {
        return verifyCanEndAttackPhase(player, false);
    }

    private boolean verifyCanEndAttackPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end attack phase, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        return true;
    }

    private void endBlockPhase(int player) {
        if (validateMoves) {
            verifyCanEndBlockPhase(player, true);
        }
        data.set(player, core.END_PHASE, 1);
        runSystems(endBlockPhaseSystems);
    }

    public boolean canEndBlockPhase(int player) {
        return verifyCanEndBlockPhase(player, false);
    }

    private boolean verifyCanEndBlockPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " is not in block phase.");
            }
            return false;
        }
        return true;
    }

    private void endMulliganPhase(int player) {
        if (validateMoves) {
            verifyCanEndMulliganPhase(player, true);
        }
        data.set(player, core.END_PHASE, 1);
        runSystems(endMulliganPhaseSystems);
    }

    public boolean canEndMulliganPhase(int player) {
        return verifyCanEndMulliganPhase(player, false);
    }

    private boolean verifyCanEndMulliganPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end mulligan phase, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        return true;
    }

    private void declareAttack(int player, int attacker, int target) {
        if (validateMoves) {
            verifyCanDeclareAttack(player, attacker, target, true);
        }
        data.set(attacker, core.ATTACKS_TARGET, target);
        // no systems, attack is only declared, nothing happens yet
    }

    public boolean canDeclareAttack(int player, int attacker, int target) {
        return verifyCanDeclareAttack(player, attacker, target, false);
    }

    private boolean verifyCanDeclareAttack(int player, int attacker, int target, boolean throwOnFail) {
        if (!data.has(target, core.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, target #" + target + " is not in battle zone.");
            }
            return false;
        }
        return verifyCanDeclareAttack(player, attacker, throwOnFail);
    }

    public boolean canDeclareAttack(int player, int attacker) {
        return verifyCanDeclareAttack(player, attacker, false);
    }

    private boolean verifyCanDeclareAttack(int player, int attacker, boolean throwOnFail) {
        if (!data.hasValue(attacker, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " does not own attacker #" + attacker + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE)) {
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
        if (data.has(attacker, core.SUMMONING_SICKNESS)) {
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

    private void block(int player, int blocker, int attacker) {
        if (validateMoves) {
            verifyCanBlock(player, blocker, attacker, true);
        }
        data.set(blocker, core.BLOCKS_ATTACKER, attacker);
        runSystems(blockSystems);
    }

    public boolean canBlock(int player, int blocker, int attacker) {
        return verifyCanBlock(player, blocker, attacker, false);
    }

    private boolean verifyCanBlock(int player, int blocker, int attacker, boolean throwOnFail) {
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
        return verifyCanBlock(player, blocker, throwOnFail);
    }

    public boolean canBlock(int player, int blocker) {
        return verifyCanBlock(player, blocker, false);
    }

    private boolean verifyCanBlock(int player, int blocker, boolean throwOnFail) {
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
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " is not in block phase.");
            }
            return false;
        }
        if (data.has(blocker, core.SUMMONING_SICKNESS)) {
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
            validateCanCast(player, castable, target != null ? target : ~0, true);
        }
        String cardName = getCardName(castable);
        try {
            data.set(castable, core.CAST_TARGET, target != null ? target : ~0);
            runSystems(castSystems);
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

    public boolean canCast(int player, int castable) {
        return validateCanCast(player, castable, false);
    }

    public boolean canCast(int player, int castable, Integer target) {
        return validateCanCast(player, castable, target != null ? target : ~0, false);
    }

    private boolean validateCanCast(int player, int castable, boolean throwOnFail) {
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
                case PlayerPhase.ATTACK_PHASE:
                    cast = template.getAttackPhaseCast();
                    if (cast == null) {
                        if (throwOnFail) {
                            throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in attack phase.");
                        }
                        return false;
                    }
                    break;
                case PlayerPhase.BLOCK_PHASE:
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

    private boolean validateCanCast(int player, int castable, int target, boolean throwOnFail) {
        CardTemplate template = settings.templates.getCard(data.get(castable, core.CARD_TEMPLATE));
        OptionalInt phase = data.getOptional(player, core.ACTIVE_PLAYER_PHASE);
        if (phase.isPresent()) {
            CardCast cast;
            if (phase.getAsInt() == PlayerPhase.ATTACK_PHASE) {
                cast = template.getAttackPhaseCast();
                if (cast != null && cast.isTargeted() && !TargetUtil.isValidTarget(data, castable, target, cast.getTargets())) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not valid.");
                    }
                    return false;
                }
            } else if (phase.getAsInt() == PlayerPhase.BLOCK_PHASE) {
                cast = template.getBlockPhaseCast();
                if (cast != null && cast.isTargeted() && !TargetUtil.isValidTarget(data, castable, target, cast.getTargets())) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not valid.");
                    }
                    return false;
                }
            }
        }
        return validateCanCast(player, castable, throwOnFail);
    }

    private void declareMulligan(int player, int card) {
        if (validateMoves) {
            verifyCanDeclareMulligan(player, card, true);
        }
        data.set(card, core.MULLIGAN, card);
        // no systems, mulligan is only declared, nothing happens yet
    }

    public boolean canDeclareMulligan(int player, int card) {
        return verifyCanDeclareMulligan(player, card, false);
    }

    private boolean verifyCanDeclareMulligan(int player, int card, boolean throwOnFail) {
        if (!data.hasValue(card, core.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " does not own card #" + card + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE)) {
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
            validateCanSurrender(player, true);
        }
        data.set(player, core.PLAYER_RESULT, PlayerResult.LOSS);
        runSystems(surrenderSystems);
    }

    public boolean canSurrender(int player) {
        return validateCanSurrender(player, false);
    }

    private boolean validateCanSurrender(int player, boolean throwOnFail) {
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

    private void runSystems(List<AbstractSystem> systems) {
        for (AbstractSystem system : systems) {
            system.run(settings, data, random);
        }
        assert validateStateLegal();
    }

    private boolean validateStateLegal() {
        IntList playerResults = data.list(core.PLAYER_RESULT);
        IntList winners = new IntList();
        IntList losers = new IntList();
        for (int player : playerResults) {
            if (data.get(player, core.PLAYER_RESULT) == PlayerResult.VICTORY) {
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
                throw new IllegalStateException();
            }
        }
        for (int player : data.list(core.ACTIVE_PLAYER_PHASE)) {
            if (data.has(player, core.PLAYER_RESULT)) {
                throw new IllegalStateException();
            }
            if (!data.has(player, core.PLAYER_INDEX)) {
                throw new IllegalStateException();
            }
        }

        if (data.list(core.ACTIVE_PLAYER_PHASE).isEmpty()) {
            if (winners.size() + losers.size() != players.size()) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (!data.has(minion, core.OWNED_BY)) {
                throw new IllegalStateException();
            }
            if (!data.has(minion, core.MINION_TEMPLATE)) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core.IN_HAND_ZONE)) {
            if (!data.has(minion, core.OWNED_BY)) {
                throw new IllegalStateException();
            }
            if (!data.has(minion, core.CARD_TEMPLATE)) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core.IN_LIBRARY_ZONE)) {
            if (!data.has(minion, core.OWNED_BY)) {
                throw new IllegalStateException();
            }
            if (!data.has(minion, core.CARD_TEMPLATE)) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core.ATTACKS_TARGET)) {
            if (!data.has(minion, core.IN_BATTLE_ZONE)) {
                throw new IllegalStateException();
            }
        }

        return true;
    }

    private boolean hasPlayerWon(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.VICTORY);
    }

    private boolean hasPlayerLost(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS);
    }

    public HistoryRandom getRandom() {
        return random;
    }
}
