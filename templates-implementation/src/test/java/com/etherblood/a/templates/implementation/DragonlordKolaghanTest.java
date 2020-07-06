package com.etherblood.a.templates.implementation;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.instances.effects.KolaghanDamageEffect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordKolaghanTest extends AbstractGameTest {

    @Test
    public void dragonlordKolaghan_damage_if_same_template_as_dead() {
        int kolaghan = createMinion(player(1), "dragonlord_kolaghan");
        int deadOrnithopter = createCard(player(0), "ornithopter", core.IN_GRAVEYARD_ZONE);
        int handOrnithopter = createCard(player(0), "ornithopter", core.IN_HAND_ZONE);

        int previousHealth = data.get(hero(0), core.HEALTH);

        game.getMoves().apply(new Cast(player(0), handOrnithopter, ~0));

        int actualHealth = data.get(hero(0), core.HEALTH);
        CardTemplate template = game.getTemplates().getCard(getCardId("dragonlord_kolaghan"));
        int expectedDamage = template.getOnSummonEffects().stream()
                .filter(KolaghanDamageEffect.class::isInstance)
                .map(KolaghanDamageEffect.class::cast)
                .mapToInt(x -> x.damage)
                .sum();
        Assertions.assertEquals(previousHealth - expectedDamage, actualHealth);
    }
}
