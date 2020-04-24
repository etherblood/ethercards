package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.systems.BlockSystem;
import com.etherblood.a.rules.systems.CastSystem;
import com.etherblood.a.rules.systems.EndAttackPhaseSystem;
import com.etherblood.a.rules.systems.EndBlockPhaseBattleSystem;
import com.etherblood.a.rules.systems.EndMulliganPhaseSystem;
import com.etherblood.a.rules.systems.UpkeepSystem;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveService {

    private static final Logger LOG = LoggerFactory.getLogger(MoveService.class);

    private final GameSettings settings;
    private final EntityData data;

    private final List<AbstractSystem> endAttackPhaseSystems;
    private final List<AbstractSystem> endBlockPhaseSystems;
    private final List<AbstractSystem> endMulliganPhaseSystems;
    private final List<AbstractSystem> blockSystems;
    private final List<AbstractSystem> castSystems;
    private final List<AbstractSystem> surrenderSystems;
    private final List<AbstractSystem> startSystems;

    public MoveService(GameSettings settings, EntityData data) {
        this.settings = settings;
        this.data = data;
        endBlockPhaseSystems = new ArrayList<>();
        endBlockPhaseSystems.add(new EndBlockPhaseBattleSystem());
        endBlockPhaseSystems.addAll(settings.generalSystems);
        endBlockPhaseSystems.add(new UpkeepSystem());
        endBlockPhaseSystems.addAll(settings.generalSystems);

        endAttackPhaseSystems = new ArrayList<>();
        endAttackPhaseSystems.add(new EndAttackPhaseSystem());
        endAttackPhaseSystems.addAll(settings.generalSystems);

        endMulliganPhaseSystems = new ArrayList<>();
        endMulliganPhaseSystems.add(new EndMulliganPhaseSystem());
        endMulliganPhaseSystems.addAll(settings.generalSystems);
        endMulliganPhaseSystems.add(new UpkeepSystem());
        endMulliganPhaseSystems.addAll(settings.generalSystems);

        blockSystems = new ArrayList<>();
        blockSystems.add(new BlockSystem());
        blockSystems.addAll(settings.generalSystems);

        castSystems = new ArrayList<>();
        castSystems.add(new CastSystem(settings.cards));
        castSystems.addAll(settings.generalSystems);

        surrenderSystems = new ArrayList<>();
        surrenderSystems.addAll(settings.generalSystems);

        startSystems = new ArrayList<>();
        startSystems.addAll(settings.generalSystems);
    }

    public void start() {
        if (settings.validateMoves) {
            verifyCanStart(true);
        }
        runWithBackup(() -> {
            for (int player : data.list(core().PLAYER_INDEX)) {
                data.set(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE);
            }
            runSystems(startSystems);
        });
    }

    public boolean canStart() {
        return verifyCanStart(false);
    }

    private boolean verifyCanStart(boolean throwOnFail) {
        if (!data.list(core().ACTIVE_PLAYER_PHASE).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, there are already active players.");
            }
            return false;
        }
        if (!data.list(core().HAS_LOST).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, at least one player already lost.");
            }
            return false;
        }
        if (!data.list(core().HAS_WON).isEmpty()) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to start game, at least one player already won.");
            }
            return false;
        }
        return true;
    }

    public void endAttackPhase(int player) {
        if (settings.validateMoves) {
            verifyCanEndAttackPhase(player, true);
        }
        runWithBackup(() -> {
            data.set(player, core().END_PHASE, 1);
            runSystems(endAttackPhaseSystems);
        });
    }

    public boolean canEndAttackPhase(int player) {
        return verifyCanEndAttackPhase(player, false);
    }

    private boolean verifyCanEndAttackPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end attack phase, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        return true;
    }

    public void endBlockPhase(int player) {
        if (settings.validateMoves) {
            verifyCanEndBlockPhase(player, true);
        }
        runWithBackup(() -> {
            data.set(player, core().END_PHASE, 1);
            runSystems(endBlockPhaseSystems);
        });
    }

    public boolean canEndBlockPhase(int player) {
        return verifyCanEndBlockPhase(player, false);
    }

    private boolean verifyCanEndBlockPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " is not in block phase.");
            }
            return false;
        }
        return true;
    }

    public void endMulliganPhase(int player) {
        if (settings.validateMoves) {
            verifyCanEndMulliganPhase(player, true);
        }
        runWithBackup(() -> {
            data.set(player, core().END_PHASE, 1);
            runSystems(endMulliganPhaseSystems);
        });
    }

    public boolean canEndMulliganPhase(int player) {
        return verifyCanEndMulliganPhase(player, false);
    }

    private boolean verifyCanEndMulliganPhase(int player, boolean throwOnFail) {
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end mulligan phase, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        return true;
    }

    public void declareAttack(int player, int attacker, int target) {
        if (settings.validateMoves) {
            verifyCanDeclareAttack(player, attacker, target, true);
        }
        runWithBackup(() -> {
            data.set(attacker, core().ATTACKS_TARGET, target);
            // no systems, attack is only declared, nothing happens yet
        });
    }

    public boolean canDeclareAttack(int player, int attacker, int target) {
        return verifyCanDeclareAttack(player, attacker, target, false);
    }

    private boolean verifyCanDeclareAttack(int player, int attacker, int target, boolean throwOnFail) {
        if (!data.has(target, core().IN_BATTLE_ZONE)) {
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
        if (!data.hasValue(attacker, core().OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " does not own attacker #" + attacker + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        if (!data.has(attacker, core().IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is not in battle zone.");
            }
            return false;
        }
        if (data.has(attacker, core().CANNOT_ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " can not attack.");
            }
            return false;
        }
        if (data.has(attacker, core().SUMMONING_SICKNESS)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " has summoning sickness.");
            }
            return false;
        }
        if (data.has(attacker, core().TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is tired.");
            }
            return false;
        }
        if (data.has(attacker, core().ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is already attacking.");
            }
            return false;
        }
        return true;
    }

    public void block(int player, int blocker, int attacker) {
        if (settings.validateMoves) {
            verifyCanBlock(player, blocker, attacker, true);
        }
        runWithBackup(() -> {
            data.set(blocker, core().BLOCKS_ATTACKER, attacker);
            runSystems(blockSystems);
        });
    }

    public boolean canBlock(int player, int blocker, int attacker) {
        return verifyCanBlock(player, blocker, attacker, false);
    }

    private boolean verifyCanBlock(int player, int blocker, int attacker, boolean throwOnFail) {
        if (!data.has(attacker, core().ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, attacker #" + attacker + " is not attacking.");
            }
            return false;
        }
        if (data.has(attacker, core().CANNOT_BE_BLOCKED)) {
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
        if (!data.hasValue(blocker, core().OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " does not own blocker #" + blocker + ".");
            }
            return false;
        }
        if (!data.has(blocker, core().IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is not in battle zone.");
            }
            return false;
        }
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " is not in block phase.");
            }
            return false;
        }
        if (data.has(blocker, core().SUMMONING_SICKNESS)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " has summoning sickness.");
            }
            return false;
        }
        if (data.has(blocker, core().TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is tired.");
            }
            return false;
        }
        if (data.has(blocker, core().CANNOT_BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " can not block.");
            }
            return false;
        }
        boolean allAttackersUnblockable = true;
        for (int attacker : data.list(core().ATTACKS_TARGET)) {
            int target = data.get(attacker, core().ATTACKS_TARGET);
            if (target == blocker) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is being attacked.");
                }
                return false;
            }
            if (allAttackersUnblockable && !data.has(attacker, core().CANNOT_BE_BLOCKED)) {
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

    public void cast(int player, int castable, Integer target) {
        if (settings.validateMoves) {
            validateCanCast(player, castable, target != null ? target : ~0, true);
        }
        runWithBackup(() -> {
            data.set(castable, core().CAST_TARGET, target != null ? target : ~0);
            runSystems(castSystems);
        });
    }

    public boolean canCast(int player, int castable) {
        return validateCanCast(player, castable, false);
    }

    public boolean canCast(int player, int castable, Integer target) {
        return validateCanCast(player, castable, target != null ? target : ~0, false);
    }

    private boolean validateCanCast(int player, int castable, boolean throwOnFail) {
        if (!data.hasValue(castable, core().OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " does not own castable #" + castable + ".");
            }
            return false;
        }
        if (!data.has(castable, core().IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + " is not in hand zone.");
            }
            return false;
        }
        CardTemplate template = settings.cards.apply(data.get(castable, core().CARD_TEMPLATE));
        CardCast cast;
        OptionalInt phase = data.getOptional(player, core().ACTIVE_PLAYER_PHASE);
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
        if (cast.getManaCost() > data.getOptional(player, core().MANA).orElse(0)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + ", player #" + player + " does not have enough mana.");
            }
            return false;
        }
        return true;
    }

    private boolean validateCanCast(int player, int castable, int target, boolean throwOnFail) {
        CardTemplate template = settings.cards.apply(data.get(castable, core().CARD_TEMPLATE));
        OptionalInt phase = data.getOptional(player, core().ACTIVE_PLAYER_PHASE);
        if (phase.isPresent()) {
            CardCast cast;
            if (phase.getAsInt() == PlayerPhase.ATTACK_PHASE) {
                cast = template.getAttackPhaseCast();
                if (cast != null && cast.isTargeted() && !data.has(target, core().IN_BATTLE_ZONE)) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not in battle zone.");
                    }
                    return false;
                }
            } else if (phase.getAsInt() == PlayerPhase.BLOCK_PHASE) {
                cast = template.getBlockPhaseCast();
                if (cast != null && cast.isTargeted() && !data.has(target, core().IN_BATTLE_ZONE)) {
                    if (throwOnFail) {
                        throw new IllegalArgumentException("Failed to cast, target #" + target + " is not in battle zone.");
                    }
                    return false;
                }
            }
        }
        return validateCanCast(player, castable, throwOnFail);
    }

    public void declareMulligan(int player, int card) {
        if (settings.validateMoves) {
            verifyCanDeclareMulligan(player, card, true);
        }
        runWithBackup(() -> {
            data.set(card, core().MULLIGAN, card);
            // no systems, mulligan is only declared, nothing happens yet
        });
    }

    public boolean canDeclareMulligan(int player, int card) {
        return verifyCanDeclareMulligan(player, card, false);
    }

    private boolean verifyCanDeclareMulligan(int player, int card, boolean throwOnFail) {
        if (!data.hasValue(card, core().OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " does not own card #" + card + ".");
            }
            return false;
        }
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, player #" + player + " is not in mulligan phase.");
            }
            return false;
        }
        if (!data.has(card, core().IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, card #" + card + " is not in hand zone.");
            }
            return false;
        }
        if (data.has(card, core().MULLIGAN)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare mulligan, card #" + card + " is already mulliganed.");
            }
            return false;
        }
        return true;
    }

    public void surrender(int player) {
        if (settings.validateMoves) {
            validateCanSurrender(player, true);
        }
        runWithBackup(() -> {
            data.set(player, core().HAS_LOST, 1);
            runSystems(surrenderSystems);
        });
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
        if (!settings.backupsEnabled) {
            runnable.run();
            return;
        }
        EntityData backup = new SimpleEntityData(data.getComponents());
        EntityUtil.copy(data, backup);
        LOG.debug("Created backup.");
        try {
            runnable.run();
        } catch (Throwable t) {
            EntityUtil.copy(backup, data);
            LOG.warn("Rolled back due to exception.");
            throw t;
        }
    }

    private void runSystems(List<AbstractSystem> systems) {
        for (AbstractSystem system : systems) {
            system.run(settings, data);
        }
//                assert validateStateLegal();
    }

    private boolean validateStateLegal() {
        IntList winners = data.list(core().HAS_WON);
        IntList players = data.list(core().PLAYER_INDEX);
        if (!winners.isEmpty()) {
            for (int player : players) {
                if (data.has(player, core().HAS_LOST) || data.has(player, core().HAS_WON)) {
                    continue;
                }
                throw new IllegalStateException();
            }
        }
        for (int player : data.list(core().ACTIVE_PLAYER_PHASE)) {
            if (data.has(player, core().HAS_LOST) || data.has(player, core().HAS_WON)) {
                throw new IllegalStateException();
            }
            if (!data.has(player, core().PLAYER_INDEX)) {
                throw new IllegalStateException();
            }
        }

        if (data.list(core().ACTIVE_PLAYER_PHASE).isEmpty()) {
            IntList losers = data.list(core().HAS_LOST);
            if (winners.size() + losers.size() != players.size()) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core().IN_BATTLE_ZONE)) {
            if (!data.has(minion, core().OWNED_BY)) {
                throw new IllegalStateException();
            }
            if (!data.has(minion, core().MINION_TEMPLATE)) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core().IN_HAND_ZONE)) {
            if (!data.has(minion, core().OWNED_BY)) {
                throw new IllegalStateException();
            }
            if (!data.has(minion, core().CARD_TEMPLATE)) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core().IN_LIBRARY_ZONE)) {
            if (!data.has(minion, core().OWNED_BY)) {
                throw new IllegalStateException();
            }
            if (!data.has(minion, core().CARD_TEMPLATE)) {
                throw new IllegalStateException();
            }
        }

        for (int minion : data.list(core().ATTACKS_TARGET)) {
            if (!data.has(minion, core().IN_BATTLE_ZONE)) {
                throw new IllegalStateException();
            }
        }

        return true;
    }

    private CoreComponents core() {
        return data.getComponents().getModule(CoreComponents.class);
    }

    private boolean hasPlayerWon(int player) {
        return data.has(player, core().HAS_WON);
    }

    private boolean hasPlayerLost(int player) {
        return data.has(player, core().HAS_LOST);
    }
}
