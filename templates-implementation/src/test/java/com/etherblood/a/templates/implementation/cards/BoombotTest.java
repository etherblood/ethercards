package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.DeathOptions;
import com.etherblood.ethercards.rules.moves.Update;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoombotTest extends AbstractGameTest {

    @Test
    public void boombot_deathrattle() {
        int boombot = createMinion(player(0), "boombot");
        int boomBotDamage = 2;
        int previousHealth = data.get(hero(1), core.HEALTH);

        data.set(boombot, core.DEATH_REQUEST, DeathOptions.NORMAL);
        moves.apply(new Update());

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - boomBotDamage, actualHealth);
    }
}
