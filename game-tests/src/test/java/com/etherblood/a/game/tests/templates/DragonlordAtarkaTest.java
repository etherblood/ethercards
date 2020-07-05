package com.etherblood.a.game.tests.templates;

import com.etherblood.a.game.tests.*;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragonlordAtarkaTest extends AbstractGameTest {

    @Test
    public void dragonlordAtarka_trample_through_ornithopter() {
        int atarka = createMinion(player(0), "dragonlord_atarka");
        int ornithopter = createMinion(player(1), "ornithopter");

        int previousHealth = data.get(hero(1), core.HEALTH);
        int atarkaAttack = data.get(atarka, core.ATTACK);

        game.getMoves().apply(new DeclareAttack(player(0), atarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new DeclareBlock(player(1), ornithopter, atarka));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertFalse(data.has(ornithopter, core.IN_BATTLE_ZONE));

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - atarkaAttack, actualHealth);
    }

    @Test
    public void dragonlordAtarka_trample_blocked_by_killing_attacker() {
        int attackingAtarka = createMinion(player(0), "dragonlord_atarka");
        int blockingAtarka = createMinion(player(1), "dragonlord_atarka");

        int previousHealth = data.get(hero(1), core.HEALTH);

        game.getMoves().apply(new DeclareAttack(player(0), attackingAtarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new DeclareBlock(player(1), blockingAtarka, attackingAtarka));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertFalse(data.has(attackingAtarka, core.IN_BATTLE_ZONE));

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth, actualHealth);
    }
}
