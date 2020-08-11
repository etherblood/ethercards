package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class YuumiTest extends AbstractGameTest {

    @Test
    public void yuumi_untargetable() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        int yuumi = createMinion(player(0), "yuumi");
        int shock_0 = createCard(player(0), "shock", core.IN_HAND_ZONE);
        int shock_1 = createCard(player(1), "shock", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), shock_0, yuumi));

        Assertions.assertTrue(data.has(yuumi, core.HEXPROOF));
        Assertions.assertTrue(data.has(yuumi, core.CANNOT_BE_ATTACKED));

        moves.apply(new EndAttackPhase(player(0)));
        Cast cast = new Cast(player(1), shock_1, yuumi);
        Assertions.assertFalse(moves.generate(true, player(1)).stream().anyMatch(cast::equals));
        Assertions.assertThrows(IllegalArgumentException.class, () -> moves.apply(cast));
    }
}
