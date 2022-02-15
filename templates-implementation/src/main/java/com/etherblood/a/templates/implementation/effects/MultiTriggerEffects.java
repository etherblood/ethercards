package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import java.util.function.IntUnaryOperator;

public class MultiTriggerEffects implements Effect {

    public final int amount;
    public final Effect[] effects;

    public MultiTriggerEffects(int amount, Effect[] effects) {
        this.amount = amount;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        for (int i = 0; i < amount; i++) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
