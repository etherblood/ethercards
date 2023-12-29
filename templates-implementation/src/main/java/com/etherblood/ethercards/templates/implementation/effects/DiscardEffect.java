package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.ZoneService;

import java.util.function.IntUnaryOperator;

public class DiscardEffect implements Effect {


    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        if (data.has(target, core.IN_HAND_ZONE)) {
            ZoneService zoneService = new ZoneService(data, templates, random, events);
            zoneService.removeFromHand(target);
            zoneService.addToGraveyard(target);
        }
    }
}
