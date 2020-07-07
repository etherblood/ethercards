package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.templates.api.filedtypes.ComponentsMap;
import java.util.function.IntUnaryOperator;

public class DebuffEffect implements Effect {

    @ComponentsMap
    public final IntMap components;

    public DebuffEffect(IntMap components) {
        this.components = components;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        for (int component : components) {
            int amount = components.get(component);
            SystemsUtil.decreaseAndRemoveLtZero(data, target, component, amount);
        }
    }
}
