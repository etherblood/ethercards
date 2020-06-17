package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class DeathSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList deaths = data.list(core.DIE);
        for (int entity : deaths) {
            data.remove(entity, core.IN_BATTLE_ZONE);
            data.remove(entity, core.OWNED_BY);
            data.remove(entity, core.ATTACKS_TARGET);
            data.remove(entity, core.ATTACK);
            data.remove(entity, core.HEALTH);
            data.remove(entity, core.TIRED);
            data.remove(entity, core.SUMMONING_SICKNESS);
            data.remove(entity, core.HERO);
            data.remove(entity, core.POISONED);
            data.remove(entity, core.VENOM);
            // TODO: full cleanup
            
            for (int attacker : data.list(core.ATTACKS_TARGET)) {
                int target = data.get(attacker, core.ATTACKS_TARGET);
                if (target == entity) {
                    data.remove(attacker, core.ATTACKS_TARGET);
                }
            }
        }
    }

}
