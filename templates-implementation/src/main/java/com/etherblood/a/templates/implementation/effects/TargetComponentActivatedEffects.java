package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.templates.api.filedtypes.ComponentId;
import java.util.function.IntUnaryOperator;

public class TargetComponentActivatedEffects implements Effect {

    @ComponentId
    public final int component;
    public final Effect[] effects;

    public TargetComponentActivatedEffects(int component, Effect[] effects) {
        this.component = component;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        if (data.has(target, component)) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
