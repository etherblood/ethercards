package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class EndAttackPhaseSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(EndAttackPhaseSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        for (int player : data.list(Components.END_ATTACK_PHASE)) {
            data.remove(player, Components.END_ATTACK_PHASE);
            data.remove(player, Components.IN_ATTACK_PHASE);
            LOG.debug("{} ended {} .",
                    entityLog(player),
                    componentLog(Components.IN_ATTACK_PHASE, 1));

            for (int attacker : data.list(Components.ATTACKS_TARGET).stream()
                    .filter(x -> data.hasValue(x, Components.OWNED_BY, player))
                    .toArray()) {
                data.set(attacker, Components.TIRED, 1);
                LOG.debug("{} is {}.", entityLog(attacker), componentLog(Components.TIRED, 1));
            }


            int nextPlayer = data.get(player, Components.NEXT_PLAYER);
            data.set(nextPlayer, Components.START_BLOCK_PHASE, 1);
        }
    }
}
