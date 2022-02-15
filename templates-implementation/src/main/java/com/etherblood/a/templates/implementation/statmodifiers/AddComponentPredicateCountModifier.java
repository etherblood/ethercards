package com.etherblood.ethercards.templates.implementation.statmodifiers;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.StatModifier;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.ComponentId;

public class AddComponentPredicateCountModifier implements StatModifier {

    @ComponentId
    public final int component;
    public final TargetPredicate predicate;

    public AddComponentPredicateCountModifier(int component, TargetPredicate predicate) {
        this.component = component;
        this.predicate = predicate;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int target, int stat) {
        for (int entity : data.list(component)) {
            if (predicate.test(data, templates, target, entity)) {
                stat++;
            }
        }
        return stat;
    }

}
