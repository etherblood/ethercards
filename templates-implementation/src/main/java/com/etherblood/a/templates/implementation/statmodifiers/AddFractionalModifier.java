package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;

public class AddFractionalModifier implements StatModifier {

    private final StatModifier modifier;
    public final int quotient, divident;

    public AddFractionalModifier(StatModifier modifier, int quotient, int divident) {
        this.modifier = modifier;
        this.quotient = quotient;
        this.divident = divident;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int target, int stat) {
        return stat + modifier.modify(data, templates, self, target, 0) * quotient / divident;
    }

}
