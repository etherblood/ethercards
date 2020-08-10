package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

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
