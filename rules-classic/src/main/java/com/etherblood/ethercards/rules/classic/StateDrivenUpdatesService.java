package com.etherblood.ethercards.rules.classic;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.DeathOptions;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.MoveAvailabilityService;
import java.util.function.IntUnaryOperator;

public class StateDrivenUpdatesService {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final EffectiveStatsService effectiveStats;

    public StateDrivenUpdatesService(EntityData data, GameTemplates templates, IntUnaryOperator random, EffectiveStatsService effectiveStats) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.effectiveStats = effectiveStats;
    }

    public void killHealthless() {
        for (int minion : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (effectiveStats.health(minion) <= 0 && !data.has(minion, core.INDESTRUCTIBLE)) {
                data.set(minion, core.DEATH_REQUEST, DeathOptions.NORMAL);
            }
        }
    }

    public void unbindFreedMinions() {
        for (int minion : data.listInValueOrder(core.BOUND_TO)) {
            int bindTarget = data.get(minion, core.BOUND_TO);
            if (!data.has(bindTarget, core.IN_BATTLE_ZONE)) {
                data.set(minion, core.OWNER, data.get(minion, core.ORIGINAL_OWNER));
                data.set(minion, core.TEAM, data.get(minion, core.ORIGINAL_TEAM));
                data.remove(minion, core.ORIGINAL_OWNER);
                data.remove(minion, core.ORIGINAL_TEAM);
                data.remove(minion, core.BOUND_TO);
            }
        }
    }

    public void removeInvalidAttacks() {
        MoveAvailabilityService moves = new MoveAvailabilityService(data, templates);
        for (int attacker : data.list(core.ATTACK_TARGET)) {
            int target = data.get(attacker, core.ATTACK_TARGET);
            if (!moves.isAttackValid(attacker, target)) {
                data.remove(attacker, core.ATTACK_TARGET);
            }
        }
    }

    public void removeInvalidBlocks() {
        MoveAvailabilityService moves = new MoveAvailabilityService(data, templates);
        for (int blocker : data.list(core.BLOCK_TARGET)) {
            int target = data.get(blocker, core.BLOCK_TARGET);
            if (!moves.isBlockValid(blocker, target)) {
                data.remove(blocker, core.BLOCK_TARGET);
            }
        }
    }

    public void attackWithRagers() {
        for (int rager : data.list(core.RAGE)) {
            declareRandomAttackIfAble(rager);
        }
    }

    private void declareRandomAttackIfAble(int attacker) {
        MoveAvailabilityService moves = new MoveAvailabilityService(data, templates);
        int owner = data.get(attacker, core.OWNER);
        if (!moves.canDeclareAttack(owner, attacker, false)) {
            return;
        }
        IntList candidates = new IntList();
        for (int candidate : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (moves.canDeclareAttack(owner, attacker, candidate, false)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        data.set(attacker, core.ATTACK_TARGET, candidates.getRandomItem(random));
    }
}
