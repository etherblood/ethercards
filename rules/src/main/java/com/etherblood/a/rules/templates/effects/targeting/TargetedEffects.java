package com.etherblood.a.rules.templates.effects.targeting;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.function.IntUnaryOperator;

public class TargetedEffects extends Effect {

    public final TargetingType targeting;
    public final TargetFilters[] targets;
    public final Effect[] effects;

    public TargetedEffects(TargetingType targeting, TargetFilters[] targets, Effect[] effects) {
        this.targeting = targeting;
        this.targets = targets;
        this.effects = effects;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, int source, int target) {
        IntList availableTargets = TargetUtil.findValidTargets(data, source, targets);
        if (availableTargets.isEmpty()) {
            return;
        }
        switch (targeting) {
            case ANY:
                int newTarget = availableTargets.getRandomItem(random);
                for (Effect effect : effects) {
                    effect.apply(settings, data, random, source, newTarget);
                }
                break;
            case ALL:
                for (Effect effect : effects) {
                    for (int availableTarget : availableTargets) {
                        effect.apply(settings, data, random, source, availableTarget);
                    }
                }
                break;
            default:
                throw new AssertionError(targeting.name());

        }
    }
}
