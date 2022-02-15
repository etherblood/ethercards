package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DruidOfTheClawTest extends AbstractGameTest {

    @Test
    public void cat() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int druid = createCard(player(0), "druid_of_the_claw", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), druid));

        CardTemplate catTemplate = templates.getCard(getAliasId("druid_of_the_claw_cat"));
        Assertions.assertEquals(catTemplate.getId(), data.get(druid, core.CARD_TEMPLATE));
        Assertions.assertEquals(catTemplate.getBattle().getComponents().get(core.ATTACK), data.get(druid, core.ATTACK));
        Assertions.assertEquals(catTemplate.getBattle().getComponents().get(core.HEALTH), data.get(druid, core.HEALTH));
        Assertions.assertEquals(catTemplate.getBattle().getComponents().hasKey(core.FAST_TO_ATTACK), data.has(druid, core.FAST_TO_ATTACK));
        Assertions.assertTrue(data.has(druid, core.SUMMONING_SICKNESS));
    }

    @Test
    public void bear() {
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        int druid = createCard(player(1), "druid_of_the_claw", core.IN_HAND_ZONE);

        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new Cast(player(1), druid));

        CardTemplate bearTemplate = templates.getCard(getAliasId("druid_of_the_claw_bear"));
        Assertions.assertEquals(bearTemplate.getId(), data.get(druid, core.CARD_TEMPLATE));
        Assertions.assertEquals(bearTemplate.getBattle().getComponents().get(core.ATTACK), data.get(druid, core.ATTACK));
        Assertions.assertEquals(bearTemplate.getBattle().getComponents().get(core.HEALTH), data.get(druid, core.HEALTH));
        Assertions.assertEquals(bearTemplate.getBattle().getComponents().hasKey(core.FAST_TO_ATTACK), data.has(druid, core.FAST_TO_ATTACK));
        Assertions.assertTrue(data.has(druid, core.SUMMONING_SICKNESS));
    }
}
