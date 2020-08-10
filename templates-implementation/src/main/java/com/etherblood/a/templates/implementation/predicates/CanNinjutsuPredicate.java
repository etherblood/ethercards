package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class CanNinjutsuPredicate implements TargetPredicate {

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        assert data.has(source, core.NINJUTSU);
        int owner = data.get(source, core.OWNER);
        return data.hasValue(target, core.OWNER, owner) 
                && !data.has(source, core.NINJUTSU_TARGET) 
                && data.list(core.NINJUTSU_TARGET).stream().noneMatch(x -> x == target);
    }
}
