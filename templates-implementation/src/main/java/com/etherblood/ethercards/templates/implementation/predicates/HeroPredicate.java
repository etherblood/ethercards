package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;

public class HeroPredicate implements TargetPredicate {

    private final boolean isHero;

    public HeroPredicate(boolean isHero) {
        this.isHero = isHero;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        return data.has(target, core.HERO) == isHero;
    }
}
