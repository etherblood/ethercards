package com.etherblood.a.game.tests;

import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GamePhaseTest extends AbstractGameTest {

    @Test
    public void end_mulliganPhase_then_player0_attackPhase() {
        data.set(player(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN);
        data.set(player(1), core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN);
        
        moves.apply(new EndMulliganPhase(player(0)));
        
        Assertions.assertFalse(data.has(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertEquals(PlayerPhase.MULLIGAN, data.get(player(1), core.ACTIVE_PLAYER_PHASE));
        
        moves.apply(new EndMulliganPhase(player(1)));
        
        Assertions.assertEquals(PlayerPhase.ATTACK, data.get(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(1), core.ACTIVE_PLAYER_PHASE));
    }
    
    @Test
    public void end_blockPhase_then_attackPhase() {
        data.set(player(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK);
        data.remove(player(1), core.ACTIVE_PLAYER_PHASE);
        
        moves.apply(new EndBlockPhase(player(0)));
        
        Assertions.assertEquals(PlayerPhase.ATTACK, data.get(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(1), core.ACTIVE_PLAYER_PHASE));
    }
    
    @Test
    public void end_attackPhase_then_opponent_blockPhase() {
        data.set(player(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK);
        data.remove(player(1), core.ACTIVE_PLAYER_PHASE);
        
        moves.apply(new EndAttackPhase(player(0)));
        
        Assertions.assertFalse(data.has(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertEquals(PlayerPhase.BLOCK, data.get(player(1), core.ACTIVE_PLAYER_PHASE));
    }
    
    @Test
    public void gameover_on_hero_death() {
        data.set(hero(0), core.DEATH_REQUEST, 1);
        moves.apply(new Update());
        
        Assertions.assertFalse(data.has(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(1), core.ACTIVE_PLAYER_PHASE));
        
        Assertions.assertEquals(PlayerResult.LOSS, data.get(player(0), core.PLAYER_RESULT));
        Assertions.assertEquals(PlayerResult.WIN, data.get(player(1), core.PLAYER_RESULT));
    }
    
}
