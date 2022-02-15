package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class SourceOwnerPredicate implements TargetPredicate {

    private final TargetPredicate predicate;

    public SourceOwnerPredicate(TargetPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNER);
        return predicate.test(data, templates, source, owner);
    }
}
