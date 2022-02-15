package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PagoTest extends AbstractGameTest {

    @Test
    public void attacksAutomatically() {
        int pago = createMinion(player(1), "pago");

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(pago, core.ATTACK_TARGET));
        Assertions.assertEquals(hero(0), data.get(pago, core.ATTACK_TARGET));
    }
}
