package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;

public class OwnHandCardCountActivatedModifier implements StatModifier {

    public final int threshold;

    public OwnHandCardCountActivatedModifier(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int stat) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNED_BY);
        int handCardCount = 0;
        for (int minion : data.list(core.IN_HAND_ZONE)) {
            if (data.hasValue(minion, core.OWNED_BY, owner)) {
                handCardCount++;
            }
        }
        if (handCardCount >= threshold) {
            return stat + 1;
        }
        return stat;
    }

}
