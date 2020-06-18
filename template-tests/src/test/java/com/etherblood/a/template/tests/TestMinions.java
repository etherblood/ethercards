package com.etherblood.a.template.tests;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.moves.EndAttackPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMinions extends AbstractGameTest {

    @Test
    public void testBoomBot() {
        Game game = game();
        EntityData data = game.getData();
        int player0 = player(game, 0);
        int boombot = summon(game, "minions/boombot.json", player0);
        data.set(boombot, core.DIE, 1);
        int hero1 = hero(game, 1);
        int previousHealth = data.get(hero1, core.HEALTH);
        game.getMoves().move(new EndAttackPhase(player0));
        int actualHealth = data.get(hero1, core.HEALTH);
        Assertions.assertNotEquals(previousHealth, actualHealth);
    }
}
