package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;

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
