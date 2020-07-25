package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MonasterySwiftspearTest extends AbstractGameTest {

    @Test
    public void prowess_on_spell_but_not_on_minion() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int swiftspear1 = createCard(player(0), "monastery_swiftspear", core.IN_HAND_ZONE);
        int coin = createCard(player(0), "the_coin", core.IN_HAND_ZONE);
        int swiftspear2 = createCard(player(0), "monastery_swiftspear", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), swiftspear1));
        moves.apply(new Cast(player(0), coin));
        moves.apply(new Cast(player(0), swiftspear2));

        CardTemplate template = templates.getCard(getCardId("monastery_swiftspear"));
        int attack = template.get(core.ATTACK);
        int health = template.get(core.HEALTH);
        Assertions.assertEquals(attack + 1, effectiveStats.attack(swiftspear1));
        Assertions.assertEquals(health + 1, effectiveStats.health(swiftspear1));
        Assertions.assertEquals(attack, effectiveStats.attack(swiftspear2));
        Assertions.assertEquals(health, effectiveStats.health(swiftspear2));
    }

    @Test
    public void no_prowess_on_opponent_coin() {
        int swiftspear = createMinion(player(1), "monastery_swiftspear");
        int coin = createCard(player(0), "the_coin", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), coin));

        CardTemplate template = templates.getCard(getCardId("monastery_swiftspear"));
        int attack = template.get(core.ATTACK);
        int health = template.get(core.HEALTH);
        Assertions.assertEquals(attack, effectiveStats.attack(swiftspear));
        Assertions.assertEquals(health, effectiveStats.health(swiftspear));
    }
}
