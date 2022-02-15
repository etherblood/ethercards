package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.UseAbility;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VoyagingSatyrTest extends AbstractGameTest {
    
    @Test
    public void satyr_gives_mana() {
        int satyr = createMinion(player(0), "voyaging_satyr");

        moves.apply(new UseAbility(player(0), satyr));

        Assertions.assertTrue(data.hasValue(player(0), core.MANA, 1));
    }
}
