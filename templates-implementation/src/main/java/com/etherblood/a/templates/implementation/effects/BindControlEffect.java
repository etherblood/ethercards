package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public class BindControlEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNED_BY);
        if (!data.has(target, core.ORIGINALLY_OWNED_BY)) {
            data.set(target, core.ORIGINALLY_OWNED_BY, data.get(target, core.OWNED_BY));
        }
        data.set(target, core.OWNED_BY, owner);
        data.set(target, core.BOUND_TO, source);
        data.set(target, core.SUMMONING_SICKNESS, 1);
    }
}
