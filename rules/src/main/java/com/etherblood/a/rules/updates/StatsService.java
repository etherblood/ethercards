package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;

public class StatsService {

    private final EntityData data;
    private final CoreComponents core;

    public StatsService(EntityData data) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
    }

    public void killHealthless() {
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (effectiveHealth(minion) <= 0) {
                data.set(minion, core.DEATH_REQUEST, 1);
            }
        }
    }

    public int effectiveHealth(int entity) {
        return data.getOptional(entity, core.HEALTH).orElse(0);
    }
    
}
