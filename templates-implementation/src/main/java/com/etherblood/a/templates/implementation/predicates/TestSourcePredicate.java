package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class TestSourcePredicate implements TargetPredicate {

    private final TargetPredicate predicate;

    public TestSourcePredicate(TargetPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return predicate.test(data, templates, source, source);
    }

}
