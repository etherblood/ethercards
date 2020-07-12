package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GoblinGuideTest extends AbstractGameTest {

    @Test
    public void goblinGuide_give_draw() {
        int goblinGuide = createMinion(player(0), "goblin_guide");
        int orniThopter = createCard(player(1), "ornithopter", core.IN_LIBRARY_ZONE);

        moves.apply(new DeclareAttack(player(0), goblinGuide, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));

        Assertions.assertTrue(data.has(orniThopter, core.IN_HAND_ZONE));
    }
}
