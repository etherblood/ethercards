package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InameDeathAspectTest extends AbstractGameTest {
    
    @Test
    public void allLibrarySpiritsToGraveyard() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int notSpirit = createCard(player(0), "ornithopter", core.IN_LIBRARY_ZONE);
        int deadOpponentSpirit = createCard(player(1), "iname_life_aspect", core.IN_LIBRARY_ZONE);
        int deadSpirit1 = createCard(player(0), "iname_life_aspect", core.IN_LIBRARY_ZONE);
        int deadSpirit2 = createCard(player(0), "iname_death_aspect", core.IN_LIBRARY_ZONE);
        
        int inameDeathAspect = createCard(player(0), "iname_death_aspect", core.IN_HAND_ZONE);
        
        moves.apply(new Cast(player(0), inameDeathAspect));
        
        Assertions.assertTrue(data.has(notSpirit, core.IN_LIBRARY_ZONE));
        Assertions.assertTrue(data.has(deadOpponentSpirit, core.IN_LIBRARY_ZONE));
        Assertions.assertTrue(data.has(deadSpirit1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(deadSpirit2, core.IN_GRAVEYARD_ZONE));
    }
}
