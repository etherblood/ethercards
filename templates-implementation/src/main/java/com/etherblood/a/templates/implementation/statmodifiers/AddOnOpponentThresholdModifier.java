package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;

public class AddOnOpponentThresholdModifier implements StatModifier {

    private final int threshold;
    private final int value;

    public AddOnOpponentThresholdModifier(int threshold, int value) {
        this.threshold = threshold;
        this.value = value;
    }

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int stat) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNER);
        IntList graveyard = data.list(core.IN_GRAVEYARD_ZONE);
        if (graveyard.size() < threshold) {
            return stat;
        }
        IntMap playerGraveCounts = new IntMap();
        for (int minion : graveyard) {
            int otherOwner = data.get(minion, core.OWNER);
            if (owner == otherOwner) {
                continue;
            }
            int graves = playerGraveCounts.getOrElse(otherOwner, 0);
            graves++;
            if (graves >= threshold) {
                return stat + value;
            }
            playerGraveCounts.set(otherOwner, graves);
        }
        return stat;
    }

}
