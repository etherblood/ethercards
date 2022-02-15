package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class MinionPredicate implements TargetPredicate {

    private final boolean isMinion;

    public MinionPredicate(boolean isMinion) {
        this.isMinion = isMinion;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int templateId = data.get(target, core.CARD_TEMPLATE);
        CardTemplate template = templates.getCard(templateId);
        return template.isMinion() == isMinion;
    }
}
