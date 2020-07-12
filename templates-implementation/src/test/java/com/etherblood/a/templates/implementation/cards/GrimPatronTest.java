package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GrimPatronTest extends AbstractGameTest {

    @Test
    public void grimPatron_onSurvive() {
        int patron = createMinion(player(0), "grim_patron");
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        data.set(patron, core.DAMAGE_REQUEST, 1);
        moves.apply(new Update());

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount + 1, actualMinionCount);
    }

    @Test
    public void grimPatron_lethal_damage_onSurvive_not_triggered() {
        int patron = createMinion(player(0), "grim_patron");
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        data.set(patron, core.DAMAGE_REQUEST, 3);
        moves.apply(new Update());

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount - 1, actualMinionCount);
    }

    @Test
    public void grimPatron_queenBees_flamestrike_onSurvive_not_triggered() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int patron = createMinion(player(1), "grim_patron");
        int beeQueen1 = createMinion(player(1), "bee_queen");
        int beeQueen2 = createMinion(player(1), "bee_queen");

        // bee queens need to buff grim patron to 5 health for this test
        Assertions.assertEquals(5, new EffectiveStatsService(data, templates).health(patron));

        int flamestrike = createCard(player(0), "flamestrike", core.IN_HAND_ZONE);
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        moves.apply(new Cast(player(0), flamestrike, null));

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount - 3, actualMinionCount);
    }
}
