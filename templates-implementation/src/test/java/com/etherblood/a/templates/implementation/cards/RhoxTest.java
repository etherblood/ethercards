package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RhoxTest extends AbstractGameTest {
    
    @Test
    public void blocked_rhox_regenerates() {
        data.set(player(0), core.MANA, 3);
        int rhox = createMinion(player(0), "rhox");
        int opponentRhox = createMinion(player(1), "rhox");
        
        moves.apply(new DeclareAttack(player(0), rhox, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));
        
        moves.apply(new DeclareBlock(player(1), opponentRhox, rhox));
        moves.apply(new EndBlockPhase(player(1)));
        
        Assertions.assertEquals(0, data.get(player(0), core.MANA));
        Assertions.assertTrue(data.has(rhox, core.TIRED));
        Assertions.assertTrue(data.has(rhox, core.IN_BATTLE_ZONE));
        Assertions.assertFalse(data.has(rhox, core.ATTACK_TARGET));
        Assertions.assertFalse(data.has(opponentRhox, core.IN_BATTLE_ZONE));
        Assertions.assertEquals(30, effectiveStats.health(hero(1)));
    }
}
