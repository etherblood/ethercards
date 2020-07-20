package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VoyagingSatyrTest extends AbstractGameTest {
    
    @Test
    public void satyr_gives_mana() {
        int satyr = createMinion(player(1), "satyr");

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.hasValue(player(1), core.MANA, 1));
    }

    @Test
    public void recently_died_satyr_gives_no_mana() {
        int satyr = createMinion(player(1), "satyr");
        int wolfrider = createMinion(player(0), "wolfrider");

        moves.apply(new DeclareAttack(player(0), wolfrider, satyr));
        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(satyr, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.hasValue(player(1), core.MANA, 0));
    }
}
