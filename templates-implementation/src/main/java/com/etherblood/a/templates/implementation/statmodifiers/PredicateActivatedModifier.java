package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.templates.api.TargetPredicate;

public class PredicateActivatedModifier implements StatModifier {

    public final TargetPredicate predicate;

    public PredicateActivatedModifier(TargetPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int stat) {
//        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
//        int owner = data.get(self, core.OWNER);
//        int handCardCount = 0;
//        for (int minion : data.list(core.IN_HAND_ZONE)) {
//            if (data.hasValue(minion, core.OWNER, owner)) {
//                handCardCount++;
//            }
//        }
//        if (handCardCount >= threshold) {
//            return stat + 1;
//        }
        if (predicate.test(data, templates, self, self)) {
            return stat + 1;
        }
        return stat;
    }

}
