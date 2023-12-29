package com.etherblood.ethercards.templates.implementation.statmodifiers;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.StatModifier;

public class AddOwnManaPoolModifier implements StatModifier {

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int target, int stat) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNER);
        int manaPool = new EffectiveStatsService(data, templates).manaPool(owner);
        return stat + manaPool;
    }

}
