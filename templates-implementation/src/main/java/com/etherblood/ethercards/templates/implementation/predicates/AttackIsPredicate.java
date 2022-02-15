package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.implementation.RelationType;

public class AttackIsPredicate implements TargetPredicate {

    private final RelationType relation;
    private final int value;

    public AttackIsPredicate(RelationType relation, int value) {
        this.relation = relation;
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        return relation.applyTo(stats.attack(target), value);
    }
}
