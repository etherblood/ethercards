package com.etherblood.a.game.tests.templates;

import com.etherblood.a.game.tests.AbstractGameTest;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordDromokaTest extends AbstractGameTest {

    @Test
    public void dragonlordDromoka_lifelink() {
        int atarka = createMinion(player(0), "dragonlord_dromoka");

        int previousHealth = data.get(hero(0), core.HEALTH);
        int dromokaAttack = data.get(atarka, core.ATTACK);

        game.getMoves().apply(new DeclareAttack(player(0), atarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new EndBlockPhase(player(1)));

        int actualHealth = data.get(hero(0), core.HEALTH);
        Assertions.assertEquals(previousHealth + dromokaAttack, actualHealth);
    }
}
