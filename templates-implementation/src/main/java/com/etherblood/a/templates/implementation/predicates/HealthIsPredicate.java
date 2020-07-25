package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.EffectiveStatsService;
import com.etherblood.a.templates.implementation.RelationType;
import com.etherblood.a.templates.api.TargetPredicate;

public class HealthIsPredicate implements TargetPredicate {

    private final RelationType relation;
    private final int value;

    public HealthIsPredicate(RelationType relation, int value) {
        this.relation = relation;
        this.value = value;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        return relation.applyTo(stats.health(target), value);
    }
}
