package com.etherblood.a.rules.templates.effects.targeting;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class TargetedEffects extends Effect {

    public final TargetingType targeting;
    public final TargetSelection[] targets;
    public final Effect[] effects;

    public TargetedEffects(TargetingType targeting, TargetSelection[] targets, Effect[] effects) {
        this.targeting = targeting;
        this.targets = targets;
        this.effects = effects;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, int source, int target) {
        List<TargetSelection> targetTypes = Arrays.asList(targets);
        assert !targetTypes.contains(null);
        IntList availableTargets = new IntList();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList list = data.list(core.IN_BATTLE_ZONE);
        int sourceOwner = data.get(source, core.OWNED_BY);
        for (int minion : list) {
            if (data.has(minion, core.DIE)) {
                continue;
            }
            int owner = data.get(minion, core.OWNED_BY);
            if (sourceOwner == owner) {
                if (data.has(minion, core.HERO)) {
                    if (targetTypes.contains(TargetSelection.OWN_HERO)) {
                        availableTargets.add(minion);
                    }
                } else {
                    if (targetTypes.contains(TargetSelection.OWN_MINION)) {
                        availableTargets.add(minion);
                    }
                }
            } else {
                if (data.has(minion, core.HERO)) {
                    if (targetTypes.contains(TargetSelection.OPPONENT_HERO)) {
                        availableTargets.add(minion);
                    }
                } else {
                    if (targetTypes.contains(TargetSelection.OPPONENT_MINION)) {
                        availableTargets.add(minion);
                    }
                }
            }
        }
        if (availableTargets.contains(source)) {
            availableTargets.swapRemove(source);
        }
        if (targetTypes.contains(TargetSelection.SOURCE)) {
            availableTargets.add(source);
        }
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
                for (int availableTarget : availableTargets) {
                    for (Effect effect : effects) {
                        effect.apply(settings, data, random, source, availableTarget);
                    }
                }
                break;
            default:
                throw new AssertionError(targeting.name());

        }
    }
}
