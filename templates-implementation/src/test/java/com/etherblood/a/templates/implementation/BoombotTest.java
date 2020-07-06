package com.etherblood.a.templates.implementation;

import com.etherblood.a.rules.moves.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoombotTest extends AbstractGameTest {

    @Test
    public void boombot_deathrattle() {
        int boombot = createMinion(player(0), "boombot");
        int boomBotDamage = 2;
        int previousHealth = data.get(hero(1), core.HEALTH);

        data.set(boombot, core.DEATH_REQUEST, 1);
        game.getMoves().apply(new Update());

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - boomBotDamage, actualHealth);
    }
}
