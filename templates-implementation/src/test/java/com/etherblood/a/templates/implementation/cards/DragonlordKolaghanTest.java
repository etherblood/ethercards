package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordKolaghanTest extends AbstractGameTest {

    @Test
    public void dragonlordKolaghan_damage_if_same_template_as_dead() {
        int kolaghan = createMinion(player(1), "dragonlord_kolaghan");
        int deadOrnithopter = createCard(player(0), "ornithopter", core.IN_GRAVEYARD_ZONE);
        int handOrnithopter = createCard(player(0), "ornithopter", core.IN_HAND_ZONE);

        int previousHealth = data.get(hero(0), core.HEALTH);

        moves.apply(new Cast(player(0), handOrnithopter, null));

        int actualHealth = data.get(hero(0), core.HEALTH);
        CardTemplate template = game.getTemplates().getCard(getAliasId("dragonlord_kolaghan"));
        int expectedDamage = 10;
        Assertions.assertEquals(previousHealth - expectedDamage, actualHealth);
    }
}
