package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.systems.util.SystemsUtil;

import java.util.Random;

public class EndBlockPhaseSystem extends AbstractSystem {

    @Override
    public void run(EntityData data, Random random) {
        for (int player : data.list(Components.END_BLOCK_PHASE)) {
            data.remove(player, Components.END_BLOCK_PHASE);
            data.remove(player, Components.IN_BLOCK_PHASE);
            for (int attacker : data.list(Components.ATTACKS_TARGET)) {
                int attackTarget = data.get(attacker, Components.ATTACKS_TARGET);
                if (data.hasValue(attackTarget, Components.OWNED_BY, player)) {
                    if (data.has(attackTarget, Components.IN_BATTLE_ZONE)) {
                        SystemsUtil.fight(data, attacker, attackTarget);
                    }
                    data.remove(attacker, Components.ATTACKS_TARGET);
                }
            }

            data.set(player, Components.START_ATTACK_PHASE, 1);
        }
    }
}
