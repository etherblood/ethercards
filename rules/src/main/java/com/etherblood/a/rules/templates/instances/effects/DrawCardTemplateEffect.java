package com.etherblood.a.rules.templates.instances.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.filedtypes.CardId;
import java.util.function.IntUnaryOperator;

public class DrawCardTemplateEffect implements Effect {

    @CardId
    public final int cardId;

    public DrawCardTemplateEffect(int cardId) {
        this.cardId = cardId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNED_BY);
        for (int card : data.list(core.IN_LIBRARY_ZONE)) {
            if (data.hasValue(card, core.CARD_TEMPLATE, cardId) && data.hasValue(card, core.OWNED_BY, owner)) {
                data.remove(card, core.IN_LIBRARY_ZONE);
                data.set(card, core.IN_HAND_ZONE, 1);
                break;
            }
        }
    }
}
