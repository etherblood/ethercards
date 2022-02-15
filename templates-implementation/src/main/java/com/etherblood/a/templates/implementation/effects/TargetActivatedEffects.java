package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import java.util.function.IntUnaryOperator;

public class TargetActivatedEffects implements Effect {

    public final Effect[] targeted;
    public final Effect[] untargeted;

    public TargetActivatedEffects(Effect[] targeted, Effect[] untargeted) {
        this.targeted = targeted;
        this.untargeted = untargeted;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        if (target == ~0) {
            for (Effect effect : untargeted) {
                effect.apply(data, templates, random, events, source, target);
            }
        } else {
            for (Effect effect : targeted) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
