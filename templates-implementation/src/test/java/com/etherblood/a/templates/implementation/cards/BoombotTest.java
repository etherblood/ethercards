package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.implementation.AbstractGameTest;
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
