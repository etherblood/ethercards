package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.UseAbility;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class YuumiTest extends AbstractGameTest {

    @Test
    public void yuumi_temporary_buffs() {
        int yuumi = createMinion(player(0), "yuumi");
        int other = createMinion(player(0), "ornithopter");

        moves.apply(new UseAbility(player(0), yuumi, other));

        Assertions.assertTrue(data.has(yuumi, core.TEMPORARY_HEXPROOF));
        Assertions.assertTrue(data.has(yuumi, core.TEMPORARY_PREVENT_COMBAT_DAMAGE));

        Assertions.assertTrue(data.has(other, core.TEMPORARY_HEALTH));
        Assertions.assertTrue(data.has(other, core.TEMPORARY_ATTACK));
    }

    @Test
    public void no_self_target() {
        int yuumi = createMinion(player(0), "yuumi");

        Assertions.assertThrows(IllegalArgumentException.class, () -> moves.apply(new UseAbility(player(0), yuumi, yuumi)));
    }
}
