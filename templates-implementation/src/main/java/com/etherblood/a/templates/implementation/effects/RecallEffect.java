package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class RecallEffect implements Effect {


    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (data.has(target, core.IN_BATTLE_ZONE)) {
            ZoneService zoneService = new ZoneService(data, templates, random, events);
            zoneService.removeFromBattle(target);
            zoneService.addToHand(target);
        }
    }
}
