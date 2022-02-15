package com.etherblood.ethercards.templates.implementation.targets;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.targeting.TargetingType;
import com.etherblood.ethercards.rules.templates.TargetSelection;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import java.util.function.IntUnaryOperator;

public class SourceTarget implements TargetSelection {

    private final boolean requiresTarget;
    private final TargetPredicate predicate;

    public SourceTarget(boolean requiresTarget, TargetPredicate predicate, TargetingType select) {
        this.requiresTarget = requiresTarget;
        this.predicate = predicate;
    }

    @Override
    public IntList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, IntList validTargets) {
        if (validTargets.isEmpty()) {
            assert !requiresTarget;
            return validTargets;
        }
        assert validTargets.size() == 1;
        return validTargets;
    }

    @Override
    public boolean requiresTarget() {
        return requiresTarget;
    }

    @Override
    public IntList getValidTargets(EntityData data, GameTemplates templates, int source) {
        if (predicate.test(data, templates, source, source)) {
            return new IntList(new int[]{source});
        }
        return new IntList();
    }

}
