package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CursedSkullTest extends AbstractGameTest {


    @Test
    public void cursedSkull() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);

        int ornithopter1 = createMinion(player(0), "ornithopter");
        int ornithopter2 = createMinion(player(0), "ornithopter");
        int ornithopter3 = createMinion(player(0), "ornithopter");
        int cursed_skull = createCard(player(0), "cursed_skull", core.IN_HAND_ZONE);

        data.set(ornithopter2, core.TIRED, 1);
        data.set(ornithopter3, core.TIRED, 1);

        moves.apply(new Cast(player(0), cursed_skull, ornithopter1));

        Assertions.assertTrue(data.has(ornithopter1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(ornithopter2, core.IN_BATTLE_ZONE));
        Assertions.assertTrue(data.has(ornithopter3, core.IN_BATTLE_ZONE));
        Assertions.assertFalse(data.has(ornithopter2, core.TIRED));
        Assertions.assertFalse(data.has(ornithopter3, core.TIRED));
    }

}
