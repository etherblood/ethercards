package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;

import java.util.function.IntUnaryOperator;

public class TargetSurvivedEffects implements Effect {

    public final Effect[] effects;

    public TargetSurvivedEffects(Effect[] effects) {
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        if (!data.has(triggerTarget, core.DEATH_REQUEST)) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, self, triggerTarget);
            }
        }
    }
}
