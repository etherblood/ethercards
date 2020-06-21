package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class RemoveDeadAbilitiesSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList deaths = data.list(core.DEATH_ACTION);
        for (int entity : deaths) {
            data.remove(entity, core.IN_BATTLE_ZONE);
            data.remove(entity, core.MANA_POOL);
            data.remove(entity, core.MANA_GROWTH);
            data.remove(entity, core.DRAWS_PER_TURN);
            data.remove(entity, core.DRAWS_ON_ATTACK);
            data.remove(entity, core.DRAWS_ON_BLOCK);
            data.remove(entity, core.DRAWS_ON_ATTACKED);
            data.remove(entity, core.ATTACKS_TARGET);
            data.remove(entity, core.OWN_MINIONS_HASTE_AURA);
            // TODO: remove all interacting components
            
        }
    }

}
