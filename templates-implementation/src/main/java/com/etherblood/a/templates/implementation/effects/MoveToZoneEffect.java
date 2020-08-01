package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.ZoneService;
import com.etherblood.a.templates.api.deserializers.filedtypes.ComponentId;
import java.util.function.IntUnaryOperator;

public class MoveToZoneEffect implements Effect {

    @ComponentId
    public final int zone;

    public MoveToZoneEffect(int zone) {
        this.zone = zone;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        ZoneService zoneService = new ZoneService(data, templates, random, events);
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (data.has(target, core.IN_HAND_ZONE)) {
            zoneService.removeFromHand(target);
        } else if (data.has(target, core.IN_LIBRARY_ZONE)) {
            zoneService.removeFromLibrary(target);
        } else if (data.has(target, core.IN_GRAVEYARD_ZONE)) {
            zoneService.removeFromGraveyard(target);
        } else {
            throw new AssertionError();
        }

        if (zone == core.IN_HAND_ZONE) {
            zoneService.addToHand(target);
        } else if (zone == core.IN_LIBRARY_ZONE) {
            zoneService.addToLibrary(target);
        } else if (zone == core.IN_GRAVEYARD_ZONE) {
            zoneService.addToGraveyard(target);
        } else {
            throw new AssertionError();
        }
    }
}
