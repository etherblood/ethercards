package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class BlockSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        for (int blocker : data.list(Components.BLOCKS_ATTACKER)) {
            int attacker = data.get(blocker, Components.BLOCKS_ATTACKER);
            SystemsUtil.fight(data, attacker, blocker);
            data.remove(attacker, Components.ATTACKS_TARGET);
            data.remove(blocker, Components.BLOCKS_ATTACKER);

            data.getOptional(blocker, Components.DRAWS_ON_BLOCK).ifPresent(draws -> {
                int owner = data.get(blocker, Components.OWNED_BY);
                SystemsUtil.increase(data, owner, Components.DRAW_CARDS, draws);
            });
        }
    }

}
