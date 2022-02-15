package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import java.util.stream.Stream;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class NoneOfPredicate implements TargetPredicate {

    private final TargetPredicate[] predicates;

    public NoneOfPredicate(TargetPredicate[] predicates) {
        this.predicates = predicates;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return Stream.of(predicates).noneMatch(predicate -> predicate.test(data, templates, source, target));
    }

}
