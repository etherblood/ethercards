package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.TriggerService;
import com.etherblood.ethercards.rules.updates.ZoneService;

import java.util.function.IntUnaryOperator;

public class SelfSummonFromLibraryEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        ZoneService zoneService = new ZoneService(data, templates, random, events);
        zoneService.removeFromLibrary(source);
        zoneService.addToBattle(source, true);
        data.set(source, core.SUMMONING_SICKNESS, 1);
        new TriggerService(data, templates, random, events).onSummoned(source);
    }
}
