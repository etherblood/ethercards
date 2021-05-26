package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class CanNinjutsuPredicate implements TargetPredicate {

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (!data.has(source, core.NINJUTSU)) {
            System.out.println(EntityUtil.extractEntityComponents(data, source));
            System.out.println(templates.getCard(data.get(source, core.CARD_TEMPLATE)).getTemplateName());
        }
        assert data.has(source, core.NINJUTSU);
        int owner = data.get(source, core.OWNER);
        return data.hasValue(target, core.OWNER, owner)
                && data.has(target, core.ATTACK_TARGET)
                && !data.has(source, core.NINJUTSU_TARGET)
                && data.list(core.NINJUTSU_TARGET).stream().noneMatch(x -> data.get(x, core.NINJUTSU_TARGET) == target);
    }
}
