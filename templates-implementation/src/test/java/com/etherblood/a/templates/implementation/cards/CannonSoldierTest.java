package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.UseAbility;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CannonSoldierTest extends AbstractGameTest {

    @Test
    public void sacrifice_for_damage() {
        int soldier = createMinion(player(0), "cannon_soldier");
        int sacrifice = createMinion(player(0), "cannon_soldier");

        int previousHealth = effectiveStats.health(hero(1));
        moves.apply(new UseAbility(player(0), soldier, sacrifice));

        Assertions.assertEquals(1, data.get(soldier, core.TIRED));
        Assertions.assertTrue(data.has(sacrifice, core.IN_GRAVEYARD_ZONE));
        Assertions.assertEquals(previousHealth - 3, effectiveStats.health(hero(1)));
    }
}
