package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.DeclareAttack;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordOjutaiTest extends AbstractGameTest {

    @Test
    public void hexproof_when_not_tired() {
        int ojutai = createMinion(player(0), "dragonlord_ojutai");

        Assertions.assertTrue(effectiveStats.isHexProof(ojutai));
        data.set(ojutai, core.TIRED, 1);
        Assertions.assertFalse(effectiveStats.isHexProof(ojutai));
    }

    @Test
    public void draw_on_fight_with_hero() {
        int ojutai = createMinion(player(0), "dragonlord_ojutai");
        int card = createCard(player(0), "dragonlord_ojutai", core.IN_LIBRARY_ZONE);

        moves.apply(new DeclareAttack(player(0), ojutai, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(card, core.IN_HAND_ZONE));
    }
}
