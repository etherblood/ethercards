package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class RemoveDeadFromBattleSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList deaths = data.list(core.DEATH_ACTION);
        for (int entity : deaths) {
            data.remove(entity, core.IN_BATTLE_ZONE);

            for (int attacker : data.list(core.ATTACKS_TARGET)) {
                int target = data.get(attacker, core.ATTACKS_TARGET);
                if (target == entity) {
                    data.remove(attacker, core.ATTACKS_TARGET);
                }
            }
        }
    }

}
