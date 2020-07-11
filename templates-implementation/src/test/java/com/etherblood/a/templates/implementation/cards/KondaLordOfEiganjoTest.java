package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KondaLordOfEiganjoTest extends AbstractGameTest {

    @Test
    public void bushido_health() {
        int konda = createMinion(player(1), "konda_lord_of_eiganjo");
        int kami = createMinion(player(0), "thousand_legged_kami");

        Assertions.assertEquals(7, data.get(kami, core.ATTACK));
        Assertions.assertEquals(3, data.get(konda, core.HEALTH));
        Assertions.assertEquals(5, data.get(konda, core.BUSHIDO));

        game.getMoves().apply(new DeclareAttack(player(0), kami, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new DeclareBlock(player(1), konda, kami));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertEquals(1, data.get(konda, core.HEALTH));
    }
}
