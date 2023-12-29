package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.implementation.RelationType;

public class HandCardCountIsPredicate implements TargetPredicate {

    private final RelationType relation;
    private final int value;

    public HandCardCountIsPredicate(RelationType relation, int value) {
        this.relation = relation;
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
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
