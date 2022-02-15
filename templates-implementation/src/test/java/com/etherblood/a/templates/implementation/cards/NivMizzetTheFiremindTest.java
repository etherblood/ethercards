package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.UseAbility;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NivMizzetTheFiremindTest extends AbstractGameTest {

    @Test
    public void draw_ping_combo() {
        int niv = createMinion(player(0), "niv_mizzet_the_firemind");
        int otherCard = createCard(player(0), "niv_mizzet_the_firemind", core.IN_LIBRARY_ZONE);
        int previousHealth = data.get(hero(1), core.HEALTH);

        moves.apply(new UseAbility(player(0), niv));

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - 1, actualHealth);
        Assertions.assertTrue(data.has(otherCard, core.IN_HAND_ZONE));
    }
}
