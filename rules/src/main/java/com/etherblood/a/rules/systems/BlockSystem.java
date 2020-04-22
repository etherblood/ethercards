package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class BlockSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int blocker : data.list(core.BLOCKS_ATTACKER)) {
            int attacker = data.get(blocker, core.BLOCKS_ATTACKER);
            SystemsUtil.fight(data, attacker, blocker);
            data.remove(attacker, core.ATTACKS_TARGET);
            data.remove(blocker, core.BLOCKS_ATTACKER);
            SystemsUtil.increase(data, blocker, core.TIRED, 1);

            data.getOptional(blocker, core.DRAWS_ON_BLOCK).ifPresent(draws -> {
                int owner = data.get(blocker, core.OWNED_BY);
                SystemsUtil.increase(data, owner, core.DRAW_CARDS, draws);
            });
        }
    }

}
