package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoneyardWurmTest extends AbstractGameTest {

    @Test
    public void allDeadSpiritsToHand() {
        createCard(player(0), "boneyard_wurm", core.IN_GRAVEYARD_ZONE);

        int wurm = createMinion(player(0), "boneyard_wurm");

        Assertions.assertEquals(1, effectiveStats.attack(wurm));
        Assertions.assertEquals(1, effectiveStats.health(wurm));

        createCard(player(0), "boneyard_wurm", core.IN_GRAVEYARD_ZONE);
        createCard(player(0), "boneyard_wurm", core.IN_GRAVEYARD_ZONE);

        Assertions.assertEquals(3, effectiveStats.attack(wurm));
        Assertions.assertEquals(3, effectiveStats.health(wurm));
    }
}
