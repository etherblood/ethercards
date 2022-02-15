package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import java.util.stream.Stream;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class AllOfPredicate implements TargetPredicate {

    private final TargetPredicate[] predicates;

    public AllOfPredicate(TargetPredicate[] predicates) {
        this.predicates = predicates;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return Stream.of(predicates).allMatch(predicate -> predicate.test(data, templates, source, target));
    }

}
