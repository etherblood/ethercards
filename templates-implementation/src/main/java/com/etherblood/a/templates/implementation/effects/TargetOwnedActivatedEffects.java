package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import java.util.function.IntUnaryOperator;

public class TargetOwnedActivatedEffects implements Effect {

    public final Effect[] effects;

    public TargetOwnedActivatedEffects(Effect[] effects) {
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int sourceOwner = data.get(source, core.OWNER);
        if (data.hasValue(target, core.OWNER, sourceOwner)) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
