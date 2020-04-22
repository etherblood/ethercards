package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.templates.effects.filedtypes.ComponentsMap;

public class BuffEffect extends Effect {

    @ComponentsMap
    public final IntMap components;

    public BuffEffect(IntMap components) {
        this.components = components;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (!data.has(target, core.IN_BATTLE_ZONE)) {
            throw new AssertionError();
        }
        if (!data.has(target, core.OWNED_BY)) {
            throw new AssertionError();
        }
        for (int component : components) {
            int value = data.getOptional(target, component).orElse(0);
            value += components.get(component);
            data.set(target, component, value);
        }
    }
}
