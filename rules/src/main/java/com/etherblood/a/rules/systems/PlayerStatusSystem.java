package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class PlayerStatusSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        updatePlayerDeaths(data);

        for (int player : data.list(Components.ACTIVE_PLAYER_PHASE)) {
            if (data.has(player, Components.HAS_LOST) || data.has(player, Components.HAS_WON)) {
                nextTurn(data, player);
                data.remove(player, Components.ACTIVE_PLAYER_PHASE);
                data.remove(player, Components.END_BLOCK_PHASE);
                data.remove(player, Components.END_ATTACK_PHASE);
            }
        }
    }

    private void nextTurn(EntityData data, int player) {
        Integer bestPlayer = SystemsUtil.nextPlayer(data, player);
        if (bestPlayer != null) {
            data.set(bestPlayer, Components.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE);
        }
    }

    private void updatePlayerDeaths(EntityData data) {
        IntList players = data.list(Components.PLAYER_INDEX);
        IntList heroes = data.list(Components.HERO);
        IntList alive = new IntList();
        for (int minion : heroes) {
            int owner = data.get(minion, Components.OWNED_BY);
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
            if (!data.has(winner, Components.HAS_WON)) {
                data.set(winner, Components.HAS_WON, 1);
            }
        }
        for (int loser : dead) {
            if (!data.has(loser, Components.HAS_LOST)) {
                data.set(loser, Components.HAS_LOST, 1);
            }
        }
    }

}
