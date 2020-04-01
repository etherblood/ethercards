package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.systems.*;
import com.etherblood.a.rules.templates.MinionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class Game {

    private static final Logger LOG = LoggerFactory.getLogger(Game.class);

    private final Random random;
    private final EntityData data;
    private final List<AbstractSystem> endAttackPhaseSystems;
    private final List<AbstractSystem> endBlockPhaseSystems;
    private final List<AbstractSystem> blockSystems;
    private final List<AbstractSystem> castSystems;
    private final List<AbstractSystem> surrenderSystems;
    private final IntFunction<CardTemplate> cards;
    private final IntFunction<MinionTemplate> minions;
    private final int[] players;
    private final boolean backupsEnabled;

    public Game(Random random, IntFunction<CardTemplate> cards, IntFunction<MinionTemplate> minions, boolean backupsEnabled) {
        this.random = random;
        this.cards = cards;
        this.minions = minions;
        this.backupsEnabled = backupsEnabled;
        data = new SimpleEntityData(Components.count());
        endBlockPhaseSystems = Arrays.asList(
                new EndBlockPhaseSystem(),
                new DamageSystem(),
                new DeathSystem(),
                new StartAttackPhaseSystem(),
                new PlayerStatusSystem(),
                new StartBlockPhaseSystem(),
                new DrawSystem()
        );
        endAttackPhaseSystems = Arrays.asList(
                new EndAttackPhaseSystem(),
                new PlayerStatusSystem(),
                new StartBlockPhaseSystem(),
                new DrawSystem()
        );
        blockSystems = Arrays.asList(
                new BlockSystem(),
                new DamageSystem(),
                new DeathSystem(),
                new PlayerStatusSystem(),
                new StartBlockPhaseSystem(),
                new DrawSystem()
        );
        castSystems = Arrays.asList(
                new CastSystem(cards, minions),
                new DamageSystem(),
                new DeathSystem(),
                new PlayerStatusSystem(),
                new StartBlockPhaseSystem(),
                new DrawSystem()
        );
        surrenderSystems = Arrays.asList(
                new PlayerStatusSystem(),
                new StartBlockPhaseSystem(),
                new DrawSystem()
        );
        int player0 = data.createEntity();
        int player1 = data.createEntity();
        if (random.nextBoolean()) {
            data.set(player0, Components.START_ATTACK_PHASE, 1);
            data.set(player0, Components.DRAW_CARDS, 3);
            data.set(player1, Components.DRAW_CARDS, 4);
        } else {
            data.set(player1, Components.START_ATTACK_PHASE, 1);
            data.set(player0, Components.DRAW_CARDS, 4);
            data.set(player1, Components.DRAW_CARDS, 3);
        }
        data.set(player0, Components.PLAYER_INDEX, 0);
        data.set(player1, Components.PLAYER_INDEX, 1);
        players = new int[]{player0, player1};
    }

    public void start() {
        List<AbstractSystem> startSystems = Arrays.asList(
                new StartAttackPhaseSystem(),
                new DrawSystem()
        );
        runSystems(startSystems);
    }

    public Random getRandom() {
        return random;
    }

    public EntityData getData() {
        return data;
    }

    public IntFunction<CardTemplate> getCards() {
        return cards;
    }

    public IntFunction<MinionTemplate> getMinions() {
        return minions;
    }

    public int getActivePlayer() {
        return IntStream.concat(data.list(Components.IN_ATTACK_PHASE).stream(), data.list(Components.IN_BLOCK_PHASE).stream()).findAny().getAsInt();
    }

    public int[] getPlayers() {
        return Arrays.copyOf(players, players.length);
    }

    public boolean isGameOver() {
        try ( Stopwatch stopwatch = TimeStats.get().time("isGameOver()")) {
            return data.list(Components.IN_ATTACK_PHASE).isEmpty() && data.list(Components.IN_BLOCK_PHASE).isEmpty();
        }
    }

    public boolean hasPlayerWon(int player) {
        return data.has(player, Components.HAS_WON);
    }

    public boolean hasPlayerLost(int player) {
        return data.has(player, Components.HAS_LOST);
    }

    public void endAttackPhase(int player) {
        verifyCanEndAttackPhase(player, true);
        runWithBackup(() -> {
            data.set(player, Components.END_ATTACK_PHASE, 1);
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
        if (!data.has(player, Components.IN_ATTACK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end attack phase, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        return true;
    }

    public void endBlockPhase(int player) {
        verifyCanEndBlockPhase(player, true);
        runWithBackup(() -> {
            data.set(player, Components.END_BLOCK_PHASE, 1);
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
        if (!data.has(player, Components.IN_BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to end block phase, player #" + player + " is not in block phase.");
            }
            return false;
        }
        return true;
    }

    public void declareAttack(int player, int attacker, int target) {
        verifyCanDeclareAttack(player, attacker, target, true);
        runWithBackup(() -> {
            data.set(attacker, Components.ATTACKS_TARGET, target);
            data.set(attacker, Components.TIRED, 1);
        });
    }

    public boolean canDeclareAttack(int player, int attacker, int target) {
        return verifyCanDeclareAttack(player, attacker, target, false);
    }

    private boolean verifyCanDeclareAttack(int player, int attacker, int target, boolean throwOnFail) {
        if (!data.has(target, Components.IN_BATTLE_ZONE)) {
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
        if (!data.hasValue(attacker, Components.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " does not own attacker #" + attacker + ".");
            }
            return false;
        }
        if (!data.has(player, Components.IN_ATTACK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, player #" + player + " is not in attack phase.");
            }
            return false;
        }
        if (!data.has(attacker, Components.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is not in battle zone.");
            }
            return false;
        }
        if (data.has(attacker, Components.CANNOT_ATTACK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " can not attack.");
            }
            return false;
        }
        if (data.has(attacker, Components.TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to declare attack, attacker #" + attacker + " is tired.");
            }
            return false;
        }
        return true;
    }

    public void block(int player, int blocker, int attacker) {
        verifyCanBlock(player, blocker, attacker, true);
        runWithBackup(() -> {
            data.set(blocker, Components.BLOCKS_ATTACKER, attacker);
            data.set(blocker, Components.TIRED, 1);
            runSystems(blockSystems);
        });
    }

    public boolean canBlock(int player, int blocker, int attacker) {
        return verifyCanBlock(player, blocker, attacker, false);
    }

    private boolean verifyCanBlock(int player, int blocker, int attacker, boolean throwOnFail) {
        if (!data.has(attacker, Components.ATTACKS_TARGET)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, attacker #" + attacker + " is not attacking.");
            }
            return false;
        }
        if (data.has(attacker, Components.CANNOT_BE_BLOCKED)) {
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
        if (!data.hasValue(blocker, Components.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " does not own blocker #" + blocker + ".");
            }
            return false;
        }
        if (!data.has(blocker, Components.IN_BATTLE_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is not in battle zone.");
            }
            return false;
        }
        if (!data.has(player, Components.IN_BLOCK_PHASE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, player #" + player + " is not in block phase.");
            }
            return false;
        }
        if (data.has(blocker, Components.TIRED)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is tired.");
            }
            return false;
        }
        if (data.has(blocker, Components.CANNOT_BLOCK)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " can not block.");
            }
            return false;
        }
        boolean allAttackersUnblockable = true;
        for (int attacker : data.list(Components.ATTACKS_TARGET)) {
            int target = data.get(attacker, Components.ATTACKS_TARGET);
            if (target == blocker) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to block, blocker #" + blocker + " is being attacked.");
                }
                return false;
            }
            if (allAttackersUnblockable && !data.has(attacker, Components.CANNOT_BE_BLOCKED)) {
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
        validateCanCast(player, castable, target != null ? target : ~0, true);
        runWithBackup(() -> {
            data.set(castable, Components.CAST_TARGET, target != null ? target : ~0);
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
        if (!data.hasValue(castable, Components.OWNED_BY, player)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " does not own castable #" + castable + ".");
            }
            return false;
        }
        if (!data.has(castable, Components.IN_HAND_ZONE)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + " is not in hand zone.");
            }
            return false;
        }
        CardTemplate template = cards.apply(data.get(castable, Components.CARD_TEMPLATE));
        CardCast cast;
        if (data.has(player, Components.IN_ATTACK_PHASE)) {
            cast = template.getAttackPhaseCast();
            if (cast == null) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in attack phase.");
                }
                return false;
            }
        } else if (data.has(player, Components.IN_BLOCK_PHASE)) {
            cast = template.getBlockPhaseCast();
            if (cast == null) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to cast, castable #" + castable + " cannot be cast in block phase.");
                }
                return false;
            }
        } else {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, player #" + player + " is not the active player.");
            }
            return false;
        }
        if (cast.getManaCost() > data.getOptional(player, Components.MANA).orElse(0)) {
            if (throwOnFail) {
                throw new IllegalArgumentException("Failed to cast, castable #" + castable + ", player #" + player + " does not have enough mana.");
            }
            return false;
        }
        return true;
    }

    private boolean validateCanCast(int player, int castable, int target, boolean throwOnFail) {
        CardTemplate template = cards.apply(data.get(castable, Components.CARD_TEMPLATE));
        CardCast cast;
        if (data.has(player, Components.IN_ATTACK_PHASE)) {
            cast = template.getAttackPhaseCast();
            if (cast != null && cast.isTargeted() && !data.has(target, Components.IN_BATTLE_ZONE)) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to cast, target #" + target + " is not in battle zone.");
                }
                return false;
            }
        } else if (data.has(player, Components.IN_BLOCK_PHASE)) {
            cast = template.getBlockPhaseCast();
            if (cast != null && cast.isTargeted() && !data.has(target, Components.IN_BATTLE_ZONE)) {
                if (throwOnFail) {
                    throw new IllegalArgumentException("Failed to cast, target #" + target + " is not in battle zone.");
                }
                return false;
            }
        }
        return validateCanCast(player, castable, throwOnFail);
    }

    public void surrender(int player) {
        validateCanSurrender(player, true);
        runWithBackup(() -> {
            data.set(player, Components.HAS_LOST, 1);
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
        if (!backupsEnabled) {
            runnable.run();
            return;
        }
        EntityData backup = new SimpleEntityData(Components.count());
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
        TimeStats stats = TimeStats.get();
        try ( Stopwatch totalStopwatch = stats.time("Systems total")) {
            for (AbstractSystem system : systems) {
                try ( Stopwatch systemStopwatch = stats.time(system.getClass().getSimpleName())) {
                    system.run(data, random);
                }
            }
        }
        assert validateStateLegal();
    }

    private boolean validateStateLegal() {
        IntList winners = data.list(Components.HAS_WON);
        IntList players = data.list(Components.PLAYER_INDEX);
        if (!winners.isEmpty()) {
            for (int player : players) {
                if (data.has(player, Components.HAS_LOST) || data.has(player, Components.HAS_WON)) {
                    continue;
                }
                throw new IllegalStateException();
            }
        }
        for (int player : data.list(Components.IN_ATTACK_PHASE)) {
            if (data.has(player, Components.HAS_LOST) || data.has(player, Components.HAS_WON)) {
                throw new IllegalStateException();
            }
        }
        for (int player : data.list(Components.IN_BLOCK_PHASE)) {
            if (data.has(player, Components.HAS_LOST) || data.has(player, Components.HAS_WON)) {
                throw new IllegalStateException();
            }
        }

        if (isGameOver()) {
            IntList losers = data.list(Components.HAS_LOST);
            if (winners.size() + losers.size() != players.size()) {
                throw new IllegalStateException();
            }
        }

        return true;
    }
}
