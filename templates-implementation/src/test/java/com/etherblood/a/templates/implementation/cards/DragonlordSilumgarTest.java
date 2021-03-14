package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordSilumgarTest extends AbstractGameTest {

    @Test
    public void dragonlordSilumgar_bindControl() {
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        int silumgar = createCard(player(1), "dragonlord_silumgar", core.IN_HAND_ZONE);
        int ornithopter = createMinion(player(0), "ornithopter");

        moves.apply(new DeclareAttack(player(0), ornithopter, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));

        moves.apply(new Cast(player(1), silumgar, ornithopter));

        Assertions.assertEquals(player(1), data.get(ornithopter, core.OWNER));
        Assertions.assertTrue(data.list(core.ATTACK_TARGET).isEmpty());
        Assertions.assertTrue(data.list(core.BLOCK_TARGET).isEmpty());
    }

    @Test
    public void dragonlordSilumgar_bindControl_polymorph_then_death() {
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        int silumgar = createCard(player(1), "dragonlord_silumgar", core.IN_HAND_ZONE);
        int polymorph = createCard(player(1), "polymorph", core.IN_HAND_ZONE);
        int ornithopter = createMinion(player(0), "ornithopter");

        moves.apply(new EndAttackPhase(player(0)));

        moves.apply(new Cast(player(1), silumgar, ornithopter));
        Assertions.assertEquals(player(1), data.get(ornithopter, core.OWNER));

        moves.apply(new Cast(player(1), polymorph, silumgar));
        Assertions.assertEquals(player(1), data.get(ornithopter, core.OWNER));

        data.set(silumgar, core.DEATH_REQUEST, DeathOptions.NORMAL);
        moves.apply(new Update());

        Assertions.assertEquals(player(0), data.get(ornithopter, core.OWNER));
    }
}
