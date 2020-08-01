package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
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
        if (predicate.test(data, templates, self, self)) {
            return stat + 1;
        }
        return stat;
    }

}
