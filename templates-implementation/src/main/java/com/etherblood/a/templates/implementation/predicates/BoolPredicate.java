package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class BoolPredicate implements TargetPredicate {

    private final boolean value;

    public BoolPredicate(boolean value) {
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return value;
    }
}
