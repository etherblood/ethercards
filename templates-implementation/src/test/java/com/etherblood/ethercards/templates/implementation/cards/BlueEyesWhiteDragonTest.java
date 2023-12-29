package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlueEyesWhiteDragonTest extends AbstractGameTest {

    @Test
    public void fusion() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int dragon1 = createMinion(player(0), "blue_eyes_white_dragon");
        int dragon2 = createMinion(player(0), "blue_eyes_white_dragon");
        int dragon3 = createCard(player(0), "blue_eyes_white_dragon", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), dragon3, null));

        Assertions.assertTrue(data.has(dragon1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(dragon2, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(dragon3, core.IN_GRAVEYARD_ZONE));

        EntityList minions = data.listInValueOrder(core.IN_BATTLE_ZONE);
        int ultimateDragon = minions.get(minions.size() - 1);

        Assertions.assertEquals(3, minions.size());
        Assertions.assertEquals(getAliasId("blue_eyes_ultimate_dragon"), data.get(ultimateDragon, core.CARD_TEMPLATE));
        Assertions.assertEquals(player(0), data.get(ultimateDragon, core.OWNER));
        Assertions.assertTrue(data.has(ultimateDragon, core.SUMMONING_SICKNESS));
    }
}
