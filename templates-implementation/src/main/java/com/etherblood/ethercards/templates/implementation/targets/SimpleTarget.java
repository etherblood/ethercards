package com.etherblood.ethercards.templates.implementation.targets;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.targeting.TargetFilters;
import com.etherblood.ethercards.rules.targeting.TargetUtil;
import com.etherblood.ethercards.rules.targeting.TargetingType;
import com.etherblood.ethercards.rules.templates.TargetSelection;

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
    public EntityList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, EntityList validTargets) {
        if (validTargets.isEmpty()) {
            assert !requiresTarget;
            return validTargets;
        }
        // user selection should be done INSTEAD of calling this method
        return switch (select) {
            case ALL -> validTargets;
            case ANY -> {
                int index = random.applyAsInt(validTargets.size());
                yield new EntityList(validTargets.get(index));
            }
            case USER ->
                // user selection should be done INSTEAD of calling this method
                    throw new AssertionError(select);
            default -> throw new AssertionError(select);
        };
    }

    @Override
    public boolean requiresTarget() {
        return requiresTarget;
    }

    @Override
    public EntityList getValidTargets(EntityData data, GameTemplates templates, int source) {
        return TargetUtil.findValidTargets(data, source, filters);
    }

}
