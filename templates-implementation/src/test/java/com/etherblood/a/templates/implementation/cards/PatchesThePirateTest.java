package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PatchesThePirateTest extends AbstractGameTest {

    @Test
    public void summonFromLibrary() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int patches = createCard(player(0), "patches_the_pirate", core.IN_LIBRARY_ZONE);
        int pirate = createCard(player(0), "patches_the_pirate", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), pirate));

        Assertions.assertTrue(data.has(patches, core.IN_BATTLE_ZONE));
    }
    
    @Test
    public void ignoresOpponentPirate() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int patches = createCard(player(1), "patches_the_pirate", core.IN_LIBRARY_ZONE);
        int pirate = createCard(player(0), "patches_the_pirate", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), pirate));

        Assertions.assertTrue(data.has(patches, core.IN_LIBRARY_ZONE));
    }
}
