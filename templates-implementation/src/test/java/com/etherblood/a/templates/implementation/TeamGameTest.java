package com.etherblood.a.templates.implementation;

import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TeamGameTest extends AbstractGameTest {

    public TeamGameTest() {
        super(2, 2);
    }

    @Test
    public void endMulliganPhase() {
        for (int team : data.list(core.ACTIVE_TEAM_PHASE)) {
            data.remove(team, core.ACTIVE_TEAM_PHASE);
        }
        for (int player : data.list(core.ACTIVE_PLAYER_PHASE)) {
            data.remove(player, core.ACTIVE_PLAYER_PHASE);
        }
        moves.apply(new Start());

        for (int player : data.list(core.PLAYER_INDEX)) {
            Assertions.assertTrue(data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN));
        }

        for (int player : data.list(core.PLAYER_INDEX)) {
            moves.apply(new EndMulliganPhase(player));
        }

        int team0 = team(0);
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.TEAM, team0)) {
                Assertions.assertTrue(data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK));
            } else {
                Assertions.assertFalse(data.has(player, core.ACTIVE_PLAYER_PHASE));
            }
        }
    }

    @Test
    public void endAttackPhase() {
        moves.apply(new EndAttackPhase(player(0)));

        Assertions.assertFalse(data.has(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertTrue(data.hasValue(player(1), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK));
        Assertions.assertFalse(data.has(player(2), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(3), core.ACTIVE_PLAYER_PHASE));

        moves.apply(new EndAttackPhase(player(1)));

        Assertions.assertFalse(data.has(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(1), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertTrue(data.hasValue(player(2), core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK));
        Assertions.assertTrue(data.hasValue(player(3), core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK));
    }

    @Test
    public void endBlockPhase() {
        data.set(team(0), core.ACTIVE_TEAM_PHASE, PlayerPhase.BLOCK);
        data.set(player(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK);
        data.set(player(1), core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK);

        moves.apply(new EndBlockPhase(player(0)));

        Assertions.assertFalse(data.has(player(0), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertTrue(data.hasValue(player(1), core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK));
        Assertions.assertFalse(data.has(player(2), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(3), core.ACTIVE_PLAYER_PHASE));

        moves.apply(new EndBlockPhase(player(1)));

        Assertions.assertTrue(data.hasValue(player(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK));
        Assertions.assertTrue(data.hasValue(player(1), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK));
        Assertions.assertFalse(data.has(player(2), core.ACTIVE_PLAYER_PHASE));
        Assertions.assertFalse(data.has(player(3), core.ACTIVE_PLAYER_PHASE));
    }

    @Test
    public void cascadeWin() {
        data.set(player(3), core.PLAYER_RESULT_REQUEST, PlayerResult.WIN);
        moves.apply(new Update());
        
        Assertions.assertEquals(PlayerResult.LOSS, data.get(player(0), core.PLAYER_RESULT));
        Assertions.assertEquals(PlayerResult.LOSS, data.get(player(1), core.PLAYER_RESULT));
        Assertions.assertEquals(PlayerResult.WIN, data.get(player(2), core.PLAYER_RESULT));
        Assertions.assertEquals(PlayerResult.WIN, data.get(player(3), core.PLAYER_RESULT));
    }

}
