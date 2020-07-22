package com.etherblood.a.templates.implementation.targets;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.targeting.TargetingType;
import com.etherblood.a.rules.templates.TargetSelection;
import java.util.function.IntUnaryOperator;

public class FlyingMinionTarget implements TargetSelection {

    private final boolean requiresTarget;
    private final TargetingType select;

    public FlyingMinionTarget(boolean requiresTarget, TargetingType select) {
        this.requiresTarget = requiresTarget;
        this.select = select;
    }

    @Override
    public IntList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, IntList validTargets) {
        if (validTargets.isEmpty()) {
            assert !requiresTarget;
            return validTargets;
        }
        switch (select) {
            case ALL:
                return validTargets;
            case ANY:
                return new IntList(new int[]{validTargets.getRandomItem(random)});
            default:
                throw new AssertionError(select);
        }
    }

    @Override
    public boolean requiresTarget() {
        return requiresTarget;
    }

    @Override
    public IntList getValidTargets(EntityData data, GameTemplates templates, int source) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList result = data.listInValueOrder(core.IN_BATTLE_ZONE);
        for (int i = result.size() - 1; i >= 0; i--) {
            if (!data.has(result.get(i), core.FLYING)) {
                result.swapRemoveAt(i);
            }
        }
        return result;
    }

}
