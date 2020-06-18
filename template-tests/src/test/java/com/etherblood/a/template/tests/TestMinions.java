package com.etherblood.a.template.tests;

import com.etherblood.a.rules.moves.EndAttackPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMinions extends AbstractGameTest {

    @Test
    public void boomBot_deathrattle() {
        int player0 = player(0);
        int boombot = summon("minions/boombot.json", player0);
        int hero1 = hero(1);
        int previousHealth = data.get(hero1, core.HEALTH);
        
        data.set(boombot, core.DIE, 1);
        game.getMoves().move(new EndAttackPhase(player0));
        
        int actualHealth = data.get(hero1, core.HEALTH);
        Assertions.assertNotEquals(previousHealth, actualHealth);
    }
}
