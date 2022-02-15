package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FelidarSovereignTest extends AbstractGameTest {

    @Test
    public void winWithHealthThreshold() {
        data.set(hero(0), core.HEALTH, 10);
        int felidar = createMinion(player(0), "felidar_sovereign");

        //effect is triggered during own upkeep
        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));
        moves.apply(new EndAttackPhase(player(1)));
        moves.apply(new EndBlockPhase(player(0)));

        Assertions.assertFalse(game.hasPlayerWon(player(0)));

        data.set(hero(0), core.HEALTH, 100);

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));
        moves.apply(new EndAttackPhase(player(1)));
        moves.apply(new EndBlockPhase(player(0)));

        Assertions.assertTrue(game.hasPlayerWon(player(0)));
    }
}
