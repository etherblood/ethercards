package com.etherblood.a.templates.implementation.targets;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.targeting.TargetingType;
import com.etherblood.a.rules.templates.TargetSelection;
import java.util.function.IntUnaryOperator;
import com.etherblood.a.templates.api.TargetPredicate;

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
