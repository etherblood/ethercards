package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.DeclareAttack;
import com.etherblood.ethercards.rules.moves.DeclareBlock;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
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

        moves.apply(new DeclareAttack(player(0), kami, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));

        moves.apply(new DeclareBlock(player(1), konda, kami));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertEquals(1, data.get(konda, core.HEALTH));
    }
}
