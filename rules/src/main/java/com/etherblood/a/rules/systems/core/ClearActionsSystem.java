package com.etherblood.a.rules.systems.core;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class ClearActionsSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList actions = new IntList();
        actions.add(core.END_PHASE_ACTION);
        actions.add(core.START_PHASE_ACTION);
        actions.add(core.DAMAGE_ACTION);
        actions.add(core.DEATH_ACTION);
        for (int action : actions) {
            for (int entity : data.list(action)) {
                data.remove(entity, action);
            }
        }
    }

}
