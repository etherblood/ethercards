package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class IsSourcePredicate implements TargetPredicate {

    private final boolean isSource;

    public IsSourcePredicate(boolean isSource) {
        this.isSource = isSource;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return (source == target) == isSource;
    }
}
