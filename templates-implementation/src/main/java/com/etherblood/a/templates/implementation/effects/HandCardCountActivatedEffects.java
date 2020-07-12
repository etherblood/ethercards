package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import java.util.function.IntUnaryOperator;

public class HandCardCountActivatedEffects implements Effect {

    public final int threshold;
    public final Effect[] effects;

    public HandCardCountActivatedEffects(int threshold, Effect[] effects) {
        this.threshold = threshold;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNER);
        int handCardCount = 0;
        for (int minion : data.list(core.IN_HAND_ZONE)) {
            if (data.hasValue(minion, core.OWNER, owner)) {
                handCardCount++;
            }
        }
        if (handCardCount >= threshold) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
