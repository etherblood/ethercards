package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordDromokaTest extends AbstractGameTest {

    @Test
    public void dragonlordDromoka_lifelink() {
        int dromoka = createMinion(player(0), "dragonlord_dromoka");

        int previousHealth = data.get(hero(0), core.HEALTH);
        int dromokaAttack = data.get(dromoka, core.ATTACK);

        moves.apply(new DeclareAttack(player(0), dromoka, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));

        moves.apply(new EndBlockPhase(player(1)));

        int actualHealth = data.get(hero(0), core.HEALTH);
        Assertions.assertEquals(previousHealth + dromokaAttack, actualHealth);
    }
}
