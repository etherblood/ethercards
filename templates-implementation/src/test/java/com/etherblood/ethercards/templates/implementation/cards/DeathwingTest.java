package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DeathwingTest extends AbstractGameTest {

    @Test
    public void destroyMinionsDiscardOwnHand() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int deathwing = createCard(player(0), "deathwing", core.IN_HAND_ZONE);
        int minion0 = createMinion(player(0), "deathwing");
        int minion1 = createMinion(player(1), "deathwing");
        int card0 = createCard(player(0), "deathwing", core.IN_HAND_ZONE);
        int card1 = createCard(player(1), "deathwing", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), deathwing));

        Assertions.assertTrue(data.has(deathwing, core.IN_BATTLE_ZONE));
        Assertions.assertTrue(data.has(minion0, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(minion1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(card0, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(card1, core.IN_HAND_ZONE));

        Assertions.assertTrue(data.has(hero(0), core.IN_BATTLE_ZONE));
        Assertions.assertTrue(data.has(hero(1), core.IN_BATTLE_ZONE));
    }

    @Disabled("intended behaviour which is currently being implemented")
    @Test
    public void does_not_trigger_lathliss() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        createMinion(player(0), "lathliss_dragon_queen");
        int deathwing = createCard(player(0), "deathwing", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), deathwing));

        Assertions.assertEquals(new IntList(hero(0), hero(1), deathwing), data.list(core.IN_BATTLE_ZONE));
    }
}
