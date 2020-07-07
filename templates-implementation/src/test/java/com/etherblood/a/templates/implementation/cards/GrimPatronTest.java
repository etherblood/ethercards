package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GrimPatronTest extends AbstractGameTest {

    @Test
    public void grimPatron_onSurvive() {
        int patron = createMinion(player(0), "grim_patron");
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        data.set(patron, core.DAMAGE_REQUEST, 1);
        game.getMoves().apply(new Update());

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount + 1, actualMinionCount);
    }

    @Test
    public void grimPatron_death_onSurvive_not_triggered() {
        int patron = createMinion(player(0), "grim_patron");
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        data.set(patron, core.DAMAGE_REQUEST, 3);
        game.getMoves().apply(new Update());

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount - 1, actualMinionCount);
    }
}
