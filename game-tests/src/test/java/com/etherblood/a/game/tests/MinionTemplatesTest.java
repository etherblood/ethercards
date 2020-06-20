package com.etherblood.a.game.tests;

import com.etherblood.a.rules.moves.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinionTemplatesTest extends AbstractGameTest {

    @Test
    public void boomBot_deathrattle() {
        int boombot = summon(player(0), "minions/boombot.json");
        int boomBotDamage = 2;
        int previousHealth = data.get(hero(1), core.HEALTH);
        
        data.set(boombot, core.DEATH_REQUEST, 1);
        game.getMoves().apply(new Update());
        
        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - boomBotDamage, actualHealth);
    }
}
