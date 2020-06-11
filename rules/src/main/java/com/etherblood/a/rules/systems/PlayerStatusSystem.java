package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class PlayerStatusSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        updatePlayerDeaths(data);

        for (int player : data.list(core.ACTIVE_PLAYER_PHASE)) {
            if (data.has(player, core.HAS_LOST) || data.has(player, core.HAS_WON)) {
                nextTurn(data, player);
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
                data.remove(player, core.END_PHASE);
            }
        }
    }

    private void nextTurn(EntityData data, int player) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        Integer bestPlayer = SystemsUtil.nextPlayer(data, player);
        if (bestPlayer != null) {
            data.set(bestPlayer, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE);
        }
    }

    private void updatePlayerDeaths(EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList players = data.list(core.PLAYER_INDEX);
        IntList heroes = data.list(core.HERO);
        IntList alive = new IntList();
        for (int minion : heroes) {
            int owner = data.get(minion, core.OWNED_BY);
            if (!alive.contains(owner) && !data.has(owner, core.HAS_LOST)) {
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
            if (!data.has(winner, core.HAS_WON)) {
                data.set(winner, core.HAS_WON, 1);
            }
        }
        for (int loser : dead) {
            if (!data.has(loser, core.HAS_LOST)) {
                data.set(loser, core.HAS_LOST, 1);
            }
        }
    }

}
