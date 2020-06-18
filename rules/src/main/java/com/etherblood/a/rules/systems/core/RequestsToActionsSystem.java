package com.etherblood.a.rules.systems.core;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class RequestsToActionsSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntMap requestToAction = new IntMap();
        requestToAction.set(core.END_PHASE_REQUEST, core.END_PHASE_ACTION);
        requestToAction.set(core.START_PHASE_REQUEST, core.START_PHASE_ACTION);
        requestToAction.set(core.DAMAGE_REQUEST, core.DAMAGE_ACTION);
        requestToAction.set(core.DEATH_REQUEST, core.DEATH_ACTION);
        
        // TODO: this is not an action and should probably have its own service
        requestToAction.set(core.PLAYER_RESULT_REQUEST, core.PLAYER_RESULT);

        for (int request : requestToAction) {
            int action = requestToAction.get(request);

            for (int entity : data.list(request)) {
                int value = data.get(entity, request);
                data.set(entity, action, value);
                data.remove(entity, request);
            }
        }
    }

}
