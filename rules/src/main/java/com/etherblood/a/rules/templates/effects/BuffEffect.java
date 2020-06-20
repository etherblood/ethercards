package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import com.etherblood.a.rules.templates.effects.filedtypes.ComponentsMap;
import java.util.function.IntUnaryOperator;

public class BuffEffect extends Effect {

    @ComponentsMap
    public final IntMap components;

    public BuffEffect(IntMap components) {
        this.components = components;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int component : components) {
            if (core.LIFELINK == component) {
                int owner = data.get(target, core.OWNED_BY);
                int hero = SystemsUtil.randomHero(data, random, owner);
                data.set(target, component, hero);
            } else {
                int amount = components.get(component);
                SystemsUtil.increase(data, target, component, amount);
            }
        }
    }
}
