package com.etherblood.a.rules.templates.effects.targeting;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.function.IntUnaryOperator;

public class TargetedEffects implements Effect {

    public final TargetingType targeting;
    public final TargetFilters[] targets;
    public final Effect[] effects;

    public TargetedEffects(TargetingType targeting, TargetFilters[] targets, Effect[] effects) {
        this.targeting = targeting;
        this.targets = targets;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        IntList availableTargets = TargetUtil.findValidTargets(data, source, targets);
        if (availableTargets.isEmpty()) {
            return;
        }
        switch (targeting) {
            case ANY:
                int newTarget = availableTargets.getRandomItem(random);
                for (Effect effect : effects) {
                    effect.apply(data, templates, random, events, source, newTarget);
                }
                break;
            case ALL:
                for (Effect effect : effects) {
                    for (int availableTarget : availableTargets) {
                        effect.apply(data, templates, random, events, source, availableTarget);
                    }
                }
                break;
            default:
                throw new AssertionError(targeting.name());

        }
    }
}
