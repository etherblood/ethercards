package com.etherblood.a.game.tests.templates;

import com.etherblood.a.game.tests.AbstractGameTest;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GoblinGuideTest extends AbstractGameTest {

    @Test
    public void goblinGuide_give_draw() {
        int goblinGuide = createMinion(player(0), "cards/goblin_guide.json");
        int orniThopter = createLibraryCard(player(1), "cards/ornithopter.json");

        game.getMoves().apply(new DeclareAttack(player(0), goblinGuide, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        Assertions.assertTrue(data.has(orniThopter, core.IN_HAND_ZONE));
    }
}
