package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LeeroyJenkinsTest extends AbstractGameTest {

    @Test
    public void leeroy_jenkins() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        data.set(player(1), core.MANA, Integer.MAX_VALUE);

        int leeroy = createCard(player(0), "leeroy_jenkins", core.IN_HAND_ZONE);

        Assertions.assertEquals(2, data.list(core.IN_BATTLE_ZONE).size());

        moves.apply(new Cast(player(0), leeroy, null));

        Assertions.assertEquals(5, data.list(core.IN_BATTLE_ZONE).size());

        int opponents = 0;
        for (int battler : data.list(core.IN_BATTLE_ZONE)) {
            if (data.hasValue(battler, core.OWNER, player(1))) {
                opponents++;
            }
        }
        Assertions.assertEquals(3, opponents);
    }
}
