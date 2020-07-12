package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PagoTest extends AbstractGameTest {

    @Test
    public void goblinGuide_give_draw() {
        int pago = createMinion(player(1), "pago");

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(pago, core.ATTACKS_TARGET));
        Assertions.assertEquals(hero(0), data.get(pago, core.ATTACKS_TARGET));
    }
}
