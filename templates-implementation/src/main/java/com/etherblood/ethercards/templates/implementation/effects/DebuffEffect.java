package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class DebuffEffect implements Effect {

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
