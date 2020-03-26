package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.systems.util.SystemsUtil;

import java.util.Random;

public class BlockSystem extends AbstractSystem {

    @Override
    public void run(EntityData data, Random random) {
        for (int blocker : data.list(Components.BLOCKS_ATTACKER)) {
            int attacker = data.get(blocker, Components.BLOCKS_ATTACKER);
            SystemsUtil.fight(data, attacker, blocker);
            data.remove(attacker, Components.ATTACKS_TARGET);
            data.remove(blocker, Components.BLOCKS_ATTACKER);
        }
    }

}
