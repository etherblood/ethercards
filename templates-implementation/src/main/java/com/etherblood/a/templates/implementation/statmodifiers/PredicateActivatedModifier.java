package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.templates.api.TargetPredicate;

public class PredicateActivatedModifier implements StatModifier {

    public final TargetPredicate predicate;
    public final int value;

    public PredicateActivatedModifier(TargetPredicate predicate, int value) {
        this.predicate = predicate;
        this.value = value;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int target, int stat) {
        assert value != 0;
        if (predicate.test(data, templates, self, target)) {
            return stat + value;
        }
        return stat;
    }

}
