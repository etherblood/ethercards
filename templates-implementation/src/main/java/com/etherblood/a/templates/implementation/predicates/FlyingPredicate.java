package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.EffectiveStatsService;
import com.etherblood.a.templates.api.TargetPredicate;

public class FlyingPredicate implements TargetPredicate {

    public final boolean isFlying;

    public FlyingPredicate(boolean isFlying) {
        this.isFlying = isFlying;
    }
    
    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        return stats.hasFlying(target) == isFlying;
    }
}
