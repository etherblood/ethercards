package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import java.util.function.IntUnaryOperator;

public class TargetHealthThresholdActivatedEffects implements Effect {

    public final int threshold;
    public final Effect[] effects;

    public TargetHealthThresholdActivatedEffects(int threshold, Effect[] effects) {
        this.threshold = threshold;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        if (stats.health(target) >= threshold) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
