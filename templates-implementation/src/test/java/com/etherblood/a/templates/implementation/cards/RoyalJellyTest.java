package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoyalJellyTest extends AbstractGameTest {

    @Test
    public void royalJelly_draw_drone() {
        int royalJelly = createCard(player(0), "royal_jelly", core.IN_HAND_ZONE);
        int queen = createCard(player(0), "bee_queen", core.IN_LIBRARY_ZONE);
        int drone = createCard(player(0), "bee_drone", core.IN_LIBRARY_ZONE);

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        moves.apply(new Cast(player(0), royalJelly, null));

        Assertions.assertTrue(data.has(queen, core.IN_LIBRARY_ZONE));
        Assertions.assertTrue(data.has(drone, core.IN_HAND_ZONE));
    }

    @Test
    public void royalJelly_draw_queen() {
        int royalJelly = createCard(player(1), "royal_jelly", core.IN_HAND_ZONE);
        int queen = createCard(player(1), "bee_queen", core.IN_LIBRARY_ZONE);
        int drone = createCard(player(1), "bee_drone", core.IN_LIBRARY_ZONE);

        moves.apply(new EndAttackPhase(player(0)));
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        moves.apply(new Cast(player(1), royalJelly, null));

        Assertions.assertTrue(data.has(queen, core.IN_HAND_ZONE));
        Assertions.assertTrue(data.has(drone, core.IN_LIBRARY_ZONE));
    }
}
