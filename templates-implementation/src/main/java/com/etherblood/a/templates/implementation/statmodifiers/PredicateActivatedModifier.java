package com.etherblood.ethercards.templates.implementation.statmodifiers;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.StatModifier;
import com.etherblood.ethercards.templates.api.TargetPredicate;

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
