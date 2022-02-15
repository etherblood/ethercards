package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import java.util.PrimitiveIterator;
import java.util.function.IntUnaryOperator;

public class PlayerDiscardEffect implements Effect {

    public final int value;

    public PlayerDiscardEffect(int value) {
        this.value = value;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        for (int player : data.list(core.IN_HAND_ZONE)) {
            IntList handCards = data.list(core.IN_HAND_ZONE);
            PrimitiveIterator.OfInt iterator = handCards.iterator();
            while (iterator.hasNext()) {
                int card = iterator.nextInt();
                if (!data.hasValue(card, core.OWNER, player)) {
                    iterator.remove();
                }
            }
            for (int i = 0; i < value && handCards.nonEmpty(); i++) {
                int index = random.applyAsInt(handCards.size());
                int card = handCards.swapRemoveAt(index);
                new DiscardEffect().apply(data, templates, random, events, source, card);
            }
        }
    }
}
