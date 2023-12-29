package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.ZoneService;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.CardId;

import java.util.function.IntUnaryOperator;

public class DrawCardTemplateEffect implements Effect {

    @CardId
    public final int cardId;

    public DrawCardTemplateEffect(int cardId) {
        this.cardId = cardId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        ZoneService zoneService = new ZoneService(data, templates, random, events);
        int owner = data.get(source, core.OWNER);
        for (int card : data.list(core.IN_LIBRARY_ZONE)) {
            if (data.hasValue(card, core.CARD_TEMPLATE, cardId) && data.hasValue(card, core.OWNER, owner)) {
                zoneService.removeFromLibrary(card);
                zoneService.addToHand(card);
                break;
            }
        }
    }
}
