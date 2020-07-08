package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Modifier;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class DiscardSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final IntUnaryOperator random;
    private final Modifier[] modifiers;
    private final Trigger[] triggers;

    public DiscardSystem(EntityData data, IntUnaryOperator random) {
        this.data = data;
        this.random = random;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.modifiers = new Modifier[0];
        this.triggers = new Trigger[0];
    }

    @Override
    public boolean isActive() {
        return data.list(core.DISCARD_CARDS_REQUEST).nonEmpty();
    }

    @Override
    public void before() {
        for (int entity : data.list(core.DISCARD_CARDS_REQUEST)) {
            int cards = data.get(entity, core.DISCARD_CARDS_REQUEST);
            for (int i = 0; cards > 0 && i < modifiers.length; i++) {
                cards = modifiers[i].modify(entity, cards);
            }
            if (cards > 0) {
                data.set(entity, core.DISCARD_CARDS_ACTION, cards);
            }
            data.remove(entity, core.DISCARD_CARDS_REQUEST);
        }
    }

    @Override
    public void run() {
        for (int player : data.list(core.DISCARD_CARDS_ACTION)) {
            int cards = data.get(player, core.DISCARD_CARDS_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(player, cards);
            }
        }
    }

    @Override
    public void after() {
        for (int player : data.list(core.DISCARD_CARDS_ACTION)) {
            int cards = data.get(player, core.DISCARD_CARDS_ACTION);
            IntList handCards = data.list(core.IN_HAND_ZONE);
            for (int i = handCards.size() - 1; i >= 0; i--) {
                if (!data.hasValue(handCards.get(i), core.OWNED_BY, player)) {
                    handCards.swapRemoveAt(i);
                }
            }
            cards = Math.min(cards, handCards.size());
            for (int i = 0; i < cards; i++) {
                int card = handCards.swapRemoveAt(random.applyAsInt(handCards.size()));
                data.remove(card, core.IN_HAND_ZONE);
                data.set(card, core.IN_GRAVEYARD_ZONE, 1);
            }
            data.remove(player, core.DISCARD_CARDS_ACTION);
        }

    }
}
