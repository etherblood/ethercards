package com.etherblood.a.templates.implementation;

import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GameMechanicsTest extends AbstractGameTest {

    @Test
    public void blocking_protects_attackTarget() {
        int attacker = createMinion(player(0), "voyaging_satyr");
        int blocker = createMinion(player(1), "voyaging_satyr");

        int previousHealth = data.get(hero(1), core.HEALTH);

        game.getMoves().apply(new DeclareAttack(player(0), attacker, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new DeclareBlock(player(1), blocker, attacker));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(attacker, core.IN_BATTLE_ZONE), "attacker must not die for this test");

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth, actualHealth);
    }
}
