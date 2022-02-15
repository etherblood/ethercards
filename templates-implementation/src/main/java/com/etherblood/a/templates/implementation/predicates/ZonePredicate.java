package com.etherblood.ethercards.templates.implementation.predicates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.TargetPredicate;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.ComponentId;

public class ZonePredicate implements TargetPredicate {

    @ComponentId
    private final int zone;
    private final boolean isInZone;

    public ZonePredicate(int zone, boolean isInZone) {
        this.zone = zone;
        this.isInZone = isInZone;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return data.has(target, zone) == isInZone;
    }
}
