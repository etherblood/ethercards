package com.etherblood.a.rules;

import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.systems.*;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class Game {

    private static final Logger LOG = LoggerFactory.getLogger(Game.class);

    private final GameSettings settings;
    private final EntityData data;
    private final List<AbstractSystem> endAttackPhaseSystems;
    private final List<AbstractSystem> endBlockPhaseSystems;
    private final List<AbstractSystem> blockSystems;
    private final List<AbstractSystem> castSystems;
    private final List<AbstractSystem> surrenderSystems;

    public Game(GameSettings settings) {
        this.settings = Objects.requireNonNull(settings, "Settings must not be null.");
        data = new SimpleEntityData(Objects.requireNonNull(settings.components, "Components missing."));
        Components components = data.getComponents();
        CoreComponents core = Objects.requireNonNull(components.getModule(CoreComponents.class), "Core component module missing.");

        endBlockPhaseSystems = new ArrayList<>();
        endBlockPhaseSystems.add(new EndBlockPhaseBattleSystem());
        endBlockPhaseSystems.addAll(settings.generalSystems);
        endBlockPhaseSystems.add(new UpkeepSystem());
        endBlockPhaseSystems.addAll(settings.generalSystems);

        endAttackPhaseSystems = new ArrayList<>();
        endAttackPhaseSystems.add(new EndAttackPhaseSystem());
        endAttackPhaseSystems.addAll(settings.generalSystems);

        blockSystems = new ArrayList<>();
        blockSystems.add(new BlockSystem());
        blockSystems.addAll(settings.generalSystems);

        castSystems = new ArrayList<>();
        castSystems.add(new CastSystem(settings.cards));
        castSystems.addAll(settings.generalSystems);

        surrenderSystems = new ArrayList<>();
        surrenderSystems.addAll(settings.generalSystems);

        int[] players = new int[settings.playerCount];
        int startingPlayerIndex = settings.random.applyAsInt(players.length);
        for (int i = 0; i < players.length; i++) {
            players[i] = data.createEntity();
            data.set(players[i], core.PLAYER_INDEX, i);
            if (startingPlayerIndex == i) {
                data.set(players[i], core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE);
                data.set(players[i], core.DRAW_CARDS, 3);
            } else {
                data.set(players[i], core.DRAW_CARDS, 4);
            }
        }
    }

    public void start() {
        endBlockPhase(getActivePlayer());
    }

    public IntUnaryOperator getRandom() {
        return settings.random;
    }

    public EntityData getData() {
        return data;
    }

    public IntFunction<CardTemplate> getCards() {
        return settings.cards;
    }

    public IntFunction<MinionTemplate> getMinions() {
        return settings.minions;
    }

    public int findPlayerByIndex(int playerIndex) {
        for (int player : data.list(core().PLAYER_INDEX)) {
            if (data.hasValue(player, core().PLAYER_INDEX, playerIndex)) {
                return player;
            }
        }
        throw new AssertionError();
    }

    public int getActivePlayerIndex() {
        return data.get(getActivePlayer(), core().PLAYER_INDEX);
    }

    public int getActivePlayer() {
        return data.list(core().ACTIVE_PLAYER_PHASE).get(0);
    }

    public boolean isGameOver() {
        return data.list(core().ACTIVE_PLAYER_PHASE).isEmpty();
    }

    public boolean hasPlayerWon(int player) {
        return data.has(player, core().HAS_WON);
    }

    public boolean hasPlayerLost(int player) {
        return data.has(player, core().HAS_LOST);
    }

    public void endAttackPhase(int player) {
        if (settings.validateMoves) {
            verifyCanEndAttackPhase(player, true);
        }
        runWithBackup(() -> {
            data.set(player, core().END_ATTACK_PHASE, 1);
            runSystems(endAttackPhaseSystems);
        });
    }

    public boolean canEndAttackPhase(int player) {
        return verifyCanEndAttackPhase(player, false);
    }

    private boolean verifyCanEndAttackPhase(int player, boolean throwOnFail) {
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end attack phase, player #" + player + " already lost.");
            }
            return false;
        }
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
            data.set(player, core().END_BLOCK_PHASE, 1);
            runSystems(endBlockPhaseSystems);
        });
    }

    public boolean canEndBlockPhase(int player) {
        return verifyCanEndBlockPhase(player, false);
    }

    private boolean verifyCanEndBlockPhase(int player, boolean throwOnFail) {
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " already lost.");
            }
            return false;
        }
        if (!data.hasValue(player, core().ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " is not in block phase.");
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
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " already lost.");
            }
            return false;
        }
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
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " already lost.");
            }
            return false;
        }
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
        if (hasPlayerLost(player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " already lost.");
            }
            return false;
        }
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
                    throw new AssertionError("Illegal phase: " + phase.getAsInt());
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
            system.run(this, data);
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

    public GameSettings getSettings() {
        return settings;
    }

    private CoreComponents core() {
        return data.getComponents().getModule(CoreComponents.class);
    }
}
