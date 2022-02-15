package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import java.util.function.IntUnaryOperator;

public class PredicateActivatedEffects implements Effect {

    public final TargetPredicate predicate;
    public final Effect[] effects;

    public PredicateActivatedEffects(TargetPredicate predicate, Effect[] effects) {
        this.predicate = predicate;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        if (predicate.test(data, templates, source, target)) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
