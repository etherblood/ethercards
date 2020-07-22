package com.etherblood.a.templates.implementation.targets;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.targeting.TargetFilters;
import com.etherblood.a.rules.targeting.TargetUtil;
import com.etherblood.a.rules.targeting.TargetingType;
import com.etherblood.a.rules.templates.TargetSelection;
import java.util.function.IntUnaryOperator;

public class SimpleTarget implements TargetSelection {

    private final boolean requiresTarget;
    private final TargetFilters[] filters;
    private final TargetingType select;

    public SimpleTarget(boolean requiresTarget, TargetFilters[] filters, TargetingType select) {
        this.requiresTarget = requiresTarget;
        this.filters = filters;
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
            case USER:
                // user selection should be done INSTEAD of calling this method
                throw new AssertionError(select);
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
        return TargetUtil.findValidTargets(data, source, filters);
    }

}
