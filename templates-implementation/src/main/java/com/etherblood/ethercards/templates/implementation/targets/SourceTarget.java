package com.etherblood.ethercards.templates.implementation.targets;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
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
    public EntityList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, EntityList validTargets) {
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
    public EntityList getValidTargets(EntityData data, GameTemplates templates, int source) {
        if (predicate.test(data, templates, source, source)) {
            return new EntityList(source);
        }
        return EntityList.EMPTY;
    }

}
