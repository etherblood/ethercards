package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class HasSourceOwnerPredicate implements TargetPredicate {

    private final boolean hasSourceOwner;

    public HasSourceOwnerPredicate(boolean hasSourceOwner) {
        this.hasSourceOwner = hasSourceOwner;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNER);
        return data.hasValue(target, core.OWNER, owner) == hasSourceOwner;
    }
}
