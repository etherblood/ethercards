package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class IsBlockedPredicate implements TargetPredicate {

    private final boolean isBlocked;

    public IsBlockedPredicate(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        return data.has(target, core.BLOCKED) == isBlocked;
    }
}
