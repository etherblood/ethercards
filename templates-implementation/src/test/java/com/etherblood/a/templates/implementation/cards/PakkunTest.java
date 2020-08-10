package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.UseAbility;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PakkunTest extends AbstractGameTest {

    @Test
    public void ninjutsu() {
        data.set(player(0), core.MANA, 1);
        int ornithopter = createMinion(player(0), "ornithopter");
        int pakkun = createCard(player(0), "pakkun", core.IN_HAND_ZONE);

        moves.apply(new DeclareAttack(player(0), ornithopter, hero(1)));
        moves.apply(new UseAbility(player(0), pakkun, ornithopter));
        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(pakkun, core.IN_BATTLE_ZONE));
        Assertions.assertTrue(data.has(ornithopter, core.IN_HAND_ZONE));
        Assertions.assertFalse(data.has(player(0), core.MANA));
    }

    @Test
    public void ninjutsu_blocked() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int ornithopter = createMinion(player(0), "ornithopter");
        int otherOrnithopter = createMinion(player(1), "ornithopter");
        int pakkun = createCard(player(0), "pakkun", core.IN_HAND_ZONE);

        moves.apply(new DeclareAttack(player(0), ornithopter, hero(1)));
        moves.apply(new UseAbility(player(0), pakkun, ornithopter));
        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new DeclareBlock(player(1), otherOrnithopter, ornithopter));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(pakkun, core.IN_HAND_ZONE));
        Assertions.assertTrue(data.has(ornithopter, core.IN_BATTLE_ZONE));
    }

    @Test
    public void ninjutsu_opponent_minion_fails() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int ornithopter = createMinion(player(0), "ornithopter");
        int pakkun = createCard(player(1), "pakkun", core.IN_HAND_ZONE);

        moves.apply(new DeclareAttack(player(0), ornithopter, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> moves.apply(new UseAbility(player(1), pakkun, ornithopter)));
    }
}
