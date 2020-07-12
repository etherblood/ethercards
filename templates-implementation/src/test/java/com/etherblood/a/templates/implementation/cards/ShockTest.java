package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShockTest extends AbstractGameTest {

    @Test
    public void boombot_deathrattle() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int shock = createCard(player(0), "shock", core.IN_HAND_ZONE);
        int shockDamage = 2;
        int previousHealth = data.get(hero(1), core.HEALTH);

        moves.apply(new Cast(player(0), shock, hero(1)));

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - shockDamage, actualHealth);
    }
}
