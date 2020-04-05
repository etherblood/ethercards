package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class DamageSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        IntList entities = data.list(Components.DAMAGE);
        for (int entity : entities) {
            if (data.has(entity, Components.IN_BATTLE_ZONE)) {
                data.getOptional(entity, Components.HEALTH).ifPresent(health -> {
                    int damage = data.get(entity, Components.DAMAGE);
                    health -= damage;
                    data.set(entity, Components.HEALTH, health);
                    if (health <= 0) {
                        data.set(entity, Components.DIE, 1);
                    } else {
                        data.getOptional(entity, Components.SUMMON_ON_SURVIVAL)
                                .ifPresent(template -> SystemsUtil.summon(game, template, data.get(entity, Components.OWNED_BY)));
                    }
                });
            }
            data.remove(entity, Components.DAMAGE);
        }
    }
}
