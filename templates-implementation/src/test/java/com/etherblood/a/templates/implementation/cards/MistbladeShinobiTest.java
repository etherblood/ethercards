package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MistbladeShinobiTest extends AbstractGameTest {

    @Disabled("intended behaviour which is currently being implemented")
    @Test
    public void does_not_return_dying_enemy() {
        int attacker0 = createMinion(player(0), "mistblade_shinobi");
        int attacker1 = createMinion(player(0), "mistblade_shinobi");
        int defender = createMinion(player(1), "mistblade_shinobi");

        moves.apply(new DeclareAttack(player(0), attacker0, hero(1)));
        moves.apply(new DeclareAttack(player(0), attacker1, defender));
        moves.apply(new EndAttackPhase(player(0)));

        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(defender, core.IN_GRAVEYARD_ZONE));
    }
}
