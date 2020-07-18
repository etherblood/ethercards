package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KiyomaroFirstToStandTest extends AbstractGameTest {

    @Test
    public void kiyomaro_vigilance() {
        createHandCard(player(0));
        int kiyomaro = createMinion(player(0), "kiyomaro_first_to_stand");
        
        Assertions.assertFalse(effectiveStats.hasVigilance(kiyomaro));
        
        int vigilanceThreshold = 4;
        for (int i = 1; i < vigilanceThreshold; i++) {
            createHandCard(player(0));
        }
        Assertions.assertTrue(effectiveStats.hasVigilance(kiyomaro));
    }

    @Test
    public void kiyomaro_heal_after_battle() {
        createHandCard(player(0));
        int kiyomaro = createMinion(player(0), "kiyomaro_first_to_stand");
        
        int previousHealth = effectiveStats.health(hero(0));
        
        moves.apply(new DeclareAttack(player(0), kiyomaro, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));
        
        Assertions.assertEquals(previousHealth, effectiveStats.health(hero(0)));
        
        int healEffectThreshold = 7;
        for (int i = 1; i < healEffectThreshold; i++) {
            createHandCard(player(0));
        }
        
        moves.apply(new EndBlockPhase(player(1)));
        moves.apply(new EndAttackPhase(player(1)));
        moves.apply(new EndBlockPhase(player(0)));
        
        moves.apply(new DeclareAttack(player(0), kiyomaro, hero(1)));
        moves.apply(new EndAttackPhase(player(0)));
        
        Assertions.assertEquals(previousHealth + healEffectThreshold, effectiveStats.health(hero(0)));
    }

    private void createHandCard(int player) {
        createCard(player, "kiyomaro_first_to_stand", core.IN_HAND_ZONE);
    }
}
