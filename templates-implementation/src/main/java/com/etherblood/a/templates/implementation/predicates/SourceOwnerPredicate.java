package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

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
