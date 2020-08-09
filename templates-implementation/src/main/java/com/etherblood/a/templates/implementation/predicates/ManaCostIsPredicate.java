package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.implementation.RelationType;
import com.etherblood.a.templates.api.TargetPredicate;

public class ManaCostIsPredicate implements TargetPredicate {

    private final RelationType relation;
    private final int value;

    public ManaCostIsPredicate(RelationType relation, int value) {
        this.relation = relation;
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int templateId = data.get(target, core.CARD_TEMPLATE);
        CardTemplate template = templates.getCard(templateId);
        Integer manaCost = template.getHand().getCast().getManaCost();
        return manaCost != null && relation.applyTo(manaCost, value);
    }
}
