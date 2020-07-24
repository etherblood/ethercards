package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.implementation.RelationType;
import com.etherblood.a.templates.api.TargetPredicate;

public class HandCardCountIsPredicate implements TargetPredicate {

    private final RelationType relation;
    private final int value;

    public HandCardCountIsPredicate(RelationType relation, int value) {
        this.relation = relation;
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        assert data.has(target, core.PLAYER_INDEX);
        int count = 0;
        for (int handCard : data.list(core.IN_HAND_ZONE)) {
            if (data.hasValue(handCard, core.OWNER, target)) {
                count++;
            }
        }
        return relation.applyTo(count, value);
    }
}
