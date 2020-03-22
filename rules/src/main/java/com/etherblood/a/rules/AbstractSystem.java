package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.systems.util.SystemsUtil;

import java.util.Random;

public abstract class AbstractSystem {

    public abstract void run(EntityData data, Random random);

    protected Object componentLog(int component, int value) {
        return SystemsUtil.componentLog(component, value);
    }

    protected Object entityLog(int entity) {
        return SystemsUtil.entityLog(entity);
    }
}
