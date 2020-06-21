package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class PlayerStatusSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        updatePlayerResults(data);

        for (int player : data.list(core.PLAYER_RESULT)) {
            if (data.has(player, core.ACTIVE_PLAYER_PHASE)) {
                nextTurn(data, player);
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
                data.remove(player, core.END_PHASE_REQUEST);
            }
        }
    }

    private void nextTurn(EntityData data, int player) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        Integer bestPlayer = SystemsUtil.nextPlayer(data, player);
        if (bestPlayer != null) {
            data.set(bestPlayer, core.START_PHASE_REQUEST, PlayerPhase.BLOCK);
        }
    }

    private void updatePlayerResults(EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList players = data.list(core.PLAYER_INDEX);
        IntList heroes = data.list(core.HERO);
        IntList alive = new IntList();
        for (int minion : heroes) {
            if (data.has(minion, core.DEATH_ACTION)) {
                continue;
            }
            int owner = data.get(minion, core.OWNED_BY);
            if (data.hasValue(owner, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                continue;
            }
            if (!alive.contains(owner)) {
                alive.add(owner);
            }
        }
        IntList dead = new IntList();
        for (int player : players) {
            if (!alive.contains(player)) {
                dead.add(player);
            }
        }

        if (alive.size() == 1) {
            int winner = alive.get(0);
            if (!data.hasValue(winner, core.PLAYER_RESULT, PlayerResult.VICTORY)) {
                data.set(winner, core.PLAYER_RESULT_REQUEST, PlayerResult.VICTORY);
            }
        }
        for (int loser : dead) {
            if (!data.hasValue(loser, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                data.set(loser, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
            }
        }
    }

}
