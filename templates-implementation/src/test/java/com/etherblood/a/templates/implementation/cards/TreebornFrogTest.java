package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TreebornFrogTest extends AbstractGameTest {

    @Test
    public void resurrectOnUpkeep() {
        int frog = createCard(player(1), "treeborn_frog", core.IN_GRAVEYARD_ZONE);

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(frog, core.IN_BATTLE_ZONE));
    }
}
