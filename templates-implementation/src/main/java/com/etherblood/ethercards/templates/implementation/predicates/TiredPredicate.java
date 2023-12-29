package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class TiredPredicate implements TargetPredicate {

    private final boolean isTired;

    public TiredPredicate(boolean isTired) {
        this.isTired = isTired;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        return data.has(target, core.TIRED) == isTired;
    }
}
