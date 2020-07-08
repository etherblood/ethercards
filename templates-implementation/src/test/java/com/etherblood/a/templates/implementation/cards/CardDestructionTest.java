package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CardDestructionTest extends AbstractGameTest {

    @Test
    public void cardDestruction_handcards_to_graveyard() {
        int cardDestruction1 = createCard(player(0), "card_destruction", core.IN_HAND_ZONE);
        int cardDestruction2 = createCard(player(0), "card_destruction", core.IN_HAND_ZONE);

        int cardDestruction3 = createCard(player(1), "card_destruction", core.IN_HAND_ZONE);
        int cardDestruction4 = createCard(player(1), "card_destruction", core.IN_HAND_ZONE);

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        game.getMoves().apply(new Cast(player(0), cardDestruction1, ~0));

        Assertions.assertTrue(data.has(cardDestruction1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(cardDestruction2, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(cardDestruction3, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(cardDestruction4, core.IN_GRAVEYARD_ZONE));
    }

}
