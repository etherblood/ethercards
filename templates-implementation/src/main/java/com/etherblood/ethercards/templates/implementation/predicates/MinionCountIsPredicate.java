package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.implementation.RelationType;

public class MinionCountIsPredicate implements TargetPredicate {

    private final RelationType relation;
    private final int value;

    public MinionCountIsPredicate(RelationType relation, int value) {
        this.relation = relation;
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        assert data.has(target, core.PLAYER_INDEX);
        int count = 0;
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.has(minion, core.HERO)) {
                continue;
            }
            if (data.hasValue(minion, core.OWNER, target)) {
                count++;
            }
        }
        return relation.applyTo(count, value);
    }
}
