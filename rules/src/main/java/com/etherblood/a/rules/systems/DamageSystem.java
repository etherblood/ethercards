package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;

public class DamageSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList entities = data.list(core.DAMAGE);
        for (int entity : entities) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                data.getOptional(entity, core.HEALTH).ifPresent(health -> {
                    int damage = data.get(entity, core.DAMAGE);
                    health -= damage;
                    data.set(entity, core.HEALTH, health);
                    if (health <= 0) {
                        data.set(entity, core.DIE, 1);
                    }
                });
            }
        }
    }
}
