package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.systems.util.SystemsUtil;

import java.util.Random;

public class EndAttackPhaseSystem extends AbstractSystem {

    @Override
    public void run(EntityData data, Random random) {
        for (int player : data.list(Components.END_ATTACK_PHASE)) {
            data.remove(player, Components.END_ATTACK_PHASE);
            data.remove(player, Components.IN_ATTACK_PHASE);

            for (int attacker : data.list(Components.ATTACKS_TARGET).stream()
                    .filter(x -> data.hasValue(x, Components.OWNED_BY, player))
                    .toArray()) {
                data.set(attacker, Components.TIRED, 1);
            }

            Integer nextPlayer = SystemsUtil.nextPlayer(data, player);
            if (nextPlayer != null) {
                data.set(nextPlayer, Components.START_BLOCK_PHASE, 1);
            }
        }
    }
}
