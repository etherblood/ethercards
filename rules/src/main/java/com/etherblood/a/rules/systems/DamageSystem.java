package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class DamageSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList entities = data.list(core.DAMAGE_ACTION);
        for (int entity : entities) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                data.getOptional(entity, core.HEALTH).ifPresent(health -> {
                    int damage = data.get(entity, core.DAMAGE_ACTION);
                    health -= damage;
                    data.set(entity, core.HEALTH, health);
                    if (health <= 0) {
                        data.set(entity, core.DEATH_REQUEST, 1);
                    }
                });
            }
        }
    }
}
