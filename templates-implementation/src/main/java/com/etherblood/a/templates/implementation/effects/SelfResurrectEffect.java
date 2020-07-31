package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.TriggerService;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class SelfResurrectEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        ZoneService zoneService = new ZoneService(data, templates, random, events);
        zoneService.removeFromGraveyard(source);
        zoneService.addToBattle(source, true);
        data.set(source, core.SUMMONING_SICKNESS, 1);
        new TriggerService(data, templates, random, events).onSummoned(source);
    }
}
