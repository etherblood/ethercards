package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.SystemsUtil;

import java.util.function.IntUnaryOperator;

public class FractionalDamageEffect implements Effect {

    public final int quotient, divident;

    public FractionalDamageEffect(int quotient, int divident) {
        this.quotient = quotient;
        this.divident = divident;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        data.getOptional(target, core.HEALTH).ifPresent(health -> {
            SystemsUtil.damage(data, events, target, health * quotient / divident);
        });
    }
}
