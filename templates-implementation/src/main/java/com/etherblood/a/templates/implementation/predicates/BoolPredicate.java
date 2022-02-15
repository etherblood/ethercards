package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

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
