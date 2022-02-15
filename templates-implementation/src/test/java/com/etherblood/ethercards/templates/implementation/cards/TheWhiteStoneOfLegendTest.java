package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.DeathOptions;
import com.etherblood.ethercards.rules.moves.Update;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import com.etherblood.ethercards.templates.implementation.effects.DiscardEffect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TheWhiteStoneOfLegendTest extends AbstractGameTest {

    @Test
    public void onDeath() {
        int stone = createMinion(player(0), "the_white_stone_of_legend");
        int dragon = createCard(player(0), "blue_eyes_white_dragon", core.IN_LIBRARY_ZONE);

        data.set(stone, core.DEATH_REQUEST, DeathOptions.NORMAL);
        moves.apply(new Update());

        Assertions.assertTrue(data.has(stone, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(dragon, core.IN_HAND_ZONE));
    }

    @Test
    public void onDiscard() {
        int stone = createCard(player(0), "the_white_stone_of_legend", core.IN_HAND_ZONE);
        int dragon = createCard(player(0), "blue_eyes_white_dragon", core.IN_LIBRARY_ZONE);

        new DiscardEffect().apply(data, templates, random, events, -1, stone);
        moves.apply(new Update());

        Assertions.assertTrue(data.has(stone, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(dragon, core.IN_HAND_ZONE));
    }
}
