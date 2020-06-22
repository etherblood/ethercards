package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class FractionalDamageEffect extends Effect {

    public final int quotient, divident;

    public FractionalDamageEffect(int quotient, int divident) {
        this.quotient = quotient;
        this.divident = divident;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.getOptional(target, core.HEALTH).ifPresent(health -> {
            SystemsUtil.damage(data, target, health * quotient / divident);
        });
    }
}
