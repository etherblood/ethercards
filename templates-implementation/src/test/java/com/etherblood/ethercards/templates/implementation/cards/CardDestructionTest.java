package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
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
        moves.apply(new Cast(player(0), cardDestruction1, null));

        Assertions.assertTrue(data.has(cardDestruction1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(cardDestruction2, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(cardDestruction3, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(cardDestruction4, core.IN_GRAVEYARD_ZONE));
    }

    @Test
    public void cardDestruction_correct_draw_count() {
        int libraryCards = 5;
        for (int i = 0; i < libraryCards; i++) {
            createCard(player(0), "card_destruction", core.IN_LIBRARY_ZONE);
            createCard(player(1), "card_destruction", core.IN_LIBRARY_ZONE);
        }

        int cardDestruction = createCard(player(0), "card_destruction", core.IN_HAND_ZONE);
        createCard(player(0), "card_destruction", core.IN_HAND_ZONE);

        createCard(player(1), "card_destruction", core.IN_HAND_ZONE);
        createCard(player(1), "card_destruction", core.IN_HAND_ZONE);

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        moves.apply(new Cast(player(0), cardDestruction, null));

        int handCards0 = 0;
        int handCards1 = 0;
        for (int card : data.list(core.IN_HAND_ZONE)) {
            if (data.hasValue(card, core.OWNER, player(0))) {
                handCards0++;
            } else {
                handCards1++;
            }
        }
        Assertions.assertEquals(1, handCards0);
        Assertions.assertEquals(2, handCards1);
    }

}
