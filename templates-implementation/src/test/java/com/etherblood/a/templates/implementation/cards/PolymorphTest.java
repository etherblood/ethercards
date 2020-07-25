package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolymorphTest extends AbstractGameTest {

    @Test
    public void transformTarget() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int dragon = createMinion(player(0), "ornithopter");
        int polymorph = createCard(player(0), "polymorph", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), polymorph, dragon));

        Assertions.assertEquals(getCardId("sheep"), data.get(dragon, core.CARD_TEMPLATE));
        Assertions.assertFalse(data.has(dragon, core.FLYING));
    }
}
