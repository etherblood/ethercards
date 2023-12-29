package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.SystemsUtil;

import java.util.function.IntUnaryOperator;

public class CardDestructionEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        IntList owners = new IntList();
        IntList cards = new IntList();
        for (int card : data.list(core.IN_HAND_ZONE)) {
            if (card == source) {
                continue;
            }
            new DiscardEffect().apply(data, templates, random, events, source, card);
            int owner = data.get(card, core.OWNER);
            int index = owners.indexOf(owner);
            if (index < 0) {
                owners.add(owner);
                cards.add(1);
            } else {
                cards.set(index, cards.get(index) + 1);
            }
        }
        for (int i = 0; i < owners.size(); i++) {
            SystemsUtil.drawCards(data, templates, random, events, owners.get(i), cards.get(i));
        }
    }
}
