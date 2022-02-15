package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

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
