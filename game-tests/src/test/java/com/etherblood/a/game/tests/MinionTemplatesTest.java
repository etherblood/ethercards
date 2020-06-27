package com.etherblood.a.game.tests;

import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinionTemplatesTest extends AbstractGameTest {

    @Test
    public void boombot_deathrattle() {
        int boombot = createMinion(player(0), "minions/boombot.json");
        int boomBotDamage = 2;
        int previousHealth = data.get(hero(1), core.HEALTH);

        data.set(boombot, core.DEATH_REQUEST, 1);
        game.getMoves().apply(new Update());

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - boomBotDamage, actualHealth);
    }
    
    @Test
    public void grimPatron_onSurvive() {
        int patron = createMinion(player(0), "minions/grim_patron.json");
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        data.set(patron, core.DAMAGE_REQUEST, 1);
        game.getMoves().apply(new Update());

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount + 1, actualMinionCount);
    }
    
    @Test
    public void grimPatron_death_onSurvive_not_triggered() {
        int patron = createMinion(player(0), "minions/grim_patron.json");
        int previousMinionCount = data.list(core.IN_BATTLE_ZONE).size();

        data.set(patron, core.DAMAGE_REQUEST, 3);
        game.getMoves().apply(new Update());

        int actualMinionCount = data.list(core.IN_BATTLE_ZONE).size();
        Assertions.assertEquals(previousMinionCount - 1, actualMinionCount);
    }

    @Test
    public void dragonlordAtarka_trample_through_ornithopter() {
        int atarka = createMinion(player(0), "minions/dragonlord_atarka.json");
        int ornithopter = createMinion(player(1), "minions/ornithopter.json");

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
        int attackingAtarka = createMinion(player(0), "minions/dragonlord_atarka.json");
        int blockingAtarka = createMinion(player(1), "minions/dragonlord_atarka.json");

        int previousHealth = data.get(hero(1), core.HEALTH);

        game.getMoves().apply(new DeclareAttack(player(0), attackingAtarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new DeclareBlock(player(1), blockingAtarka, attackingAtarka));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertFalse(data.has(attackingAtarka, core.IN_BATTLE_ZONE));

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth, actualHealth);
    }

    @Test
    public void dragonlordDromoka_lifelink() {
        int atarka = createMinion(player(0), "minions/dragonlord_dromoka.json");

        int previousHealth = data.get(hero(0), core.HEALTH);
        int dromokaAttack = data.get(atarka, core.ATTACK);

        game.getMoves().apply(new DeclareAttack(player(0), atarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new EndBlockPhase(player(1)));

        int actualHealth = data.get(hero(0), core.HEALTH);
        Assertions.assertEquals(previousHealth + dromokaAttack, actualHealth);
    }

    @Test
    public void armadilloCloak() {
        int armadilloCloakHealth = 2;
        int armadilloCloakAttack = 2;

        int ornithopter = createMinion(player(0), "minions/ornithopter.json");
        int armadillo_cloak = createHandCard(player(0), "cards/armadillo_cloak.json");

        int previousHealth = data.get(ornithopter, core.HEALTH);
        int previousAttack = data.get(ornithopter, core.ATTACK);

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        game.getMoves().apply(new Cast(player(0), armadillo_cloak, ornithopter));

        int actualHealth = data.get(ornithopter, core.HEALTH);
        int actualAttack = data.get(ornithopter, core.ATTACK);

        Assertions.assertEquals(previousHealth + armadilloCloakHealth, actualHealth);
        Assertions.assertEquals(previousAttack + armadilloCloakAttack, actualAttack);
        Assertions.assertTrue(data.has(ornithopter, core.TRAMPLE));
        Assertions.assertTrue(data.has(ornithopter, core.LIFELINK));
    }

    @Test
    public void goblinGuide_give_draw() {
        int goblinGuide = createMinion(player(0), "minions/goblin_guide.json");
        int orniThopter = createLibraryCard(player(1), "minions/ornithopter.json");

        game.getMoves().apply(new DeclareAttack(player(0), goblinGuide, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));
        
        Assertions.assertTrue(data.has(orniThopter, core.IN_HAND_ZONE));
    }

    @Test
    public void blocking_protects_attackTarget() {
        int attacker = createMinion(player(0), "minions/satyr.json");
        int blocker = createMinion(player(1), "minions/satyr.json");

        int previousHealth = data.get(hero(1), core.HEALTH);

        game.getMoves().apply(new DeclareAttack(player(0), attacker, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new DeclareBlock(player(1), blocker, attacker));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.has(attacker, core.IN_BATTLE_ZONE), "attacker must not die for this test");

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth, actualHealth);
    }
}
