package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class StartBlockPhaseSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(StartBlockPhaseSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        IntList entities = data.list(Components.START_BLOCK_PHASE);
        for (int entity : entities) {
            data.remove(entity, Components.START_BLOCK_PHASE);
            data.set(entity, Components.IN_BLOCK_PHASE, 1);
            LOG.debug("{} started {} .",
                    entityLog(entity),
                    componentLog(Components.IN_BLOCK_PHASE, 1));
        }
    }
}
