package com.etherblood.a.rules.templates.instances.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class FractionalDamageEffect implements Effect {

    public final int quotient, divident;

    public FractionalDamageEffect(int quotient, int divident) {
        this.quotient = quotient;
        this.divident = divident;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.getOptional(target, core.HEALTH).ifPresent(health -> {
            SystemsUtil.damage(data, target, health * quotient / divident);
        });
    }
}
