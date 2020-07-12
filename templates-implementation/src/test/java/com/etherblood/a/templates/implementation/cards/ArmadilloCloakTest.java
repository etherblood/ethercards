package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArmadilloCloakTest extends AbstractGameTest {


    @Test
    public void armadilloCloak() {
        int armadilloCloakHealth = 2;
        int armadilloCloakAttack = 2;

        int ornithopter = createMinion(player(0), "ornithopter");
        int armadillo_cloak = createCard(player(0), "armadillo_cloak", core.IN_HAND_ZONE);

        int previousHealth = data.get(ornithopter, core.HEALTH);
        int previousAttack = data.get(ornithopter, core.ATTACK);

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        moves.apply(new Cast(player(0), armadillo_cloak, ornithopter));

        int actualHealth = data.get(ornithopter, core.HEALTH);
        int actualAttack = data.get(ornithopter, core.ATTACK);

        Assertions.assertEquals(previousHealth + armadilloCloakHealth, actualHealth);
        Assertions.assertEquals(previousAttack + armadilloCloakAttack, actualAttack);
        Assertions.assertTrue(data.has(ornithopter, core.TRAMPLE));
        Assertions.assertTrue(data.has(ornithopter, core.LIFELINK));
    }

}
