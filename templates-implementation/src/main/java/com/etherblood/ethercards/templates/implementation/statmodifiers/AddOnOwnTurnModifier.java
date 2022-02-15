package com.etherblood.ethercards.templates.implementation.statmodifiers;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.StatModifier;

public class AddOnOwnTurnModifier implements StatModifier {

    private final int value;

    public AddOnOwnTurnModifier(int value) {
        this.value = value;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int target, int stat) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNER);
        if (data.has(owner, core.ACTIVE_PLAYER_PHASE)) {
            return stat + value;
        }
        return stat;
    }

}
