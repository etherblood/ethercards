package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Tribe;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class HasTribePredicate implements TargetPredicate {

    public final Tribe tribe;

    public HasTribePredicate(Tribe tribe) {
        this.tribe = tribe;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int templateId = data.get(target, core.CARD_TEMPLATE);
        CardTemplate template = templates.getCard(templateId);
        return template.getTribes().contains(tribe);
    }
}
