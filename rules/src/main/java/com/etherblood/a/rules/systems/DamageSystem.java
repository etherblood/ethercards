package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DamageSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DamageSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        IntList entities = data.list(Components.DAMAGE);
        for (int entity : entities) {
            data.getOptional(entity, Components.HEALTH).ifPresent(health -> {
                int damage = data.get(entity, Components.DAMAGE);
                health -= damage;
                data.set(entity, Components.HEALTH, health);
                LOG.info("{} took {} and has {} now.",
                        entityLog(entity),
                        componentLog(Components.DAMAGE, damage),
                        componentLog(Components.HEALTH, health));
                if(health <= 0) {
                    data.set(entity, Components.DIE, 1);
                }
            });
            data.remove(entity, Components.DAMAGE);
        }
    }
}
