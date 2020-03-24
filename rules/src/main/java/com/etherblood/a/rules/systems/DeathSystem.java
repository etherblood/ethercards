package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DeathSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DeathSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        IntList deaths = data.list(Components.DIE);
        for (int entity : deaths) {
            LOG.info("{} died.", entityLog(entity));
            data.remove(entity, Components.IN_BATTLE_ZONE);
            data.remove(entity, Components.OWNED_BY);
            data.remove(entity, Components.DIE);
        }
    }

}
