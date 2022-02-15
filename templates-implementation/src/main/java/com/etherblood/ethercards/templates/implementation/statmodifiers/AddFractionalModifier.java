package com.etherblood.ethercards.templates.implementation.statmodifiers;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.StatModifier;

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
