package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class CanNinjutsuPredicate implements TargetPredicate {

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        assert data.has(source, core.NINJUTSU);
        int owner = data.get(source, core.OWNER);
        return data.hasValue(target, core.OWNER, owner)
                && data.has(target, core.ATTACK_TARGET)
                && !data.has(source, core.NINJUTSU_TARGET)
                && data.list(core.NINJUTSU_TARGET).stream().noneMatch(x -> data.get(x, core.NINJUTSU_TARGET) == target);
    }
}
