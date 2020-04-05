package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.casteffects.filedtypes.ComponentsMap;

public class BuffEffect extends CastEffect {

    @ComponentsMap
    public final IntMap components;

    public BuffEffect(IntMap components) {
        this.components = components;
    }

    @Override
    public void cast(Game game, EntityData data, int source, int target) {
        if (!data.has(target, Components.IN_BATTLE_ZONE)) {
            throw new AssertionError();
        }
        if (!data.has(target, Components.OWNED_BY)) {
            throw new AssertionError();
        }
        for (int component : components) {
            int value = data.getOptional(target, component).orElse(0);
            value += components.get(component);
            data.set(target, component, value);
        }
    }
}
