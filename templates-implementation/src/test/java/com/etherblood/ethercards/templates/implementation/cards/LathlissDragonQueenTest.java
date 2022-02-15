package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LathlissDragonQueenTest extends AbstractGameTest {

    @Test
    public void lathliss_summon_dragon_token() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        data.set(player(1), core.MANA, Integer.MAX_VALUE);

        int lathliss = createMinion(player(0), "lathliss_dragon_queen");
        int babyDragon0 = createCard(player(0), "baby_dragon", core.IN_HAND_ZONE);
        int babyDragon1 = createCard(player(1), "baby_dragon", core.IN_HAND_ZONE);

        Assertions.assertEquals(3, data.list(core.IN_BATTLE_ZONE).size());

        moves.apply(new Cast(player(0), babyDragon0, null));

        Assertions.assertEquals(5, data.list(core.IN_BATTLE_ZONE).size());

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new Cast(player(1), babyDragon1, null));

        Assertions.assertEquals(6, data.list(core.IN_BATTLE_ZONE).size());
    }
}
