package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class IsBlockedPredicate implements TargetPredicate {

    private final boolean isBlocked;

    public IsBlockedPredicate(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        return data.has(target, core.BLOCKED) == isBlocked;
    }
}
