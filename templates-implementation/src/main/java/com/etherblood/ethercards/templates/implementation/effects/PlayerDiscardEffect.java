package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;

import java.util.function.IntUnaryOperator;

public class PlayerDiscardEffect implements Effect {

    public final int value;

    public PlayerDiscardEffect(int value) {
        this.value = value;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);

        for (int player : data.list(core.IN_HAND_ZONE)) {
            EntityList cards = data.list(core.IN_HAND_ZONE);

            IntList ownHandCards = new IntList();
            for (int card : cards) {
                if (data.hasValue(card, core.OWNER, player)) {
                    ownHandCards.add(card);
                }
            }
            for (int i = 0; i < value && ownHandCards.nonEmpty(); i++) {
                int index = random.applyAsInt(ownHandCards.size());
                int card = ownHandCards.swapRemoveAt(index);
                new DiscardEffect().apply(data, templates, random, events, source, card);
            }
        }
    }
}
