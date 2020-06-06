package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class TemporariesCleanupSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int entity : data.list(core.DAMAGE)) {
            data.remove(entity, core.DAMAGE);
        }
        for (int entity : data.list(core.DIE)) {
            data.remove(entity, core.DIE);
        }
    }
}
