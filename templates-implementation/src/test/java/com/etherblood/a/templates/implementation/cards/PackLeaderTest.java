package com.etherblood.ethercards.templates.implementation.cards;

import com.etherblood.ethercards.rules.moves.Cast;
import com.etherblood.ethercards.rules.moves.DeclareAttack;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PackLeaderTest extends AbstractGameTest {

    @Test
    public void prevent_combat_damage_on_attack() {
        int leader = createMinion(player(0), "pack_leader");
        int dog = createMinion(player(0), "dog_token");

        int opponentDog = createMinion(player(1), "dog_token");
        data.set(opponentDog, core.ATTACK, 100);

        moves.apply(new DeclareAttack(player(0), leader, opponentDog));
        moves.apply(new DeclareAttack(player(0), dog, opponentDog));
        moves.apply(new EndAttackPhase(player(0)));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(leader, core.IN_BATTLE_ZONE));
        Assertions.assertTrue(data.has(dog, core.IN_BATTLE_ZONE));
    }

    @Test
    public void take_fireball_damage_on_attack() {
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        int leader = createMinion(player(0), "pack_leader");
        int dog = createMinion(player(0), "dog_token");

        int fireball = createCard(player(1), "fireball", core.IN_HAND_ZONE);
        int opponentDog = createMinion(player(1), "dog_token");
        data.set(opponentDog, core.ATTACK, 100);

        moves.apply(new DeclareAttack(player(0), leader, opponentDog));
        moves.apply(new DeclareAttack(player(0), dog, opponentDog));
        moves.apply(new EndAttackPhase(player(0)));

        moves.apply(new Cast(player(1), fireball, leader));
        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertFalse(data.has(leader, core.IN_BATTLE_ZONE));
        Assertions.assertFalse(data.has(dog, core.IN_BATTLE_ZONE));
    }
}
