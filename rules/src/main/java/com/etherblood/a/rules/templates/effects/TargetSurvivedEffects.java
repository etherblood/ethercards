package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public class TargetSurvivedEffects implements Effect {

    public final Effect[] effects;

    public TargetSurvivedEffects(Effect[] effects) {
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (!data.has(triggerTarget, core.DEATH_REQUEST)) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, self, triggerTarget);
            }
        }
    }
}
