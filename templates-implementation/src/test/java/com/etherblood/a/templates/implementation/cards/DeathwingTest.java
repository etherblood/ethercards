package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
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
}
