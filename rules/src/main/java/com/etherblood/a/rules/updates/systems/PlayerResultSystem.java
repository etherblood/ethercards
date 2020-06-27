package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Modifier;
import com.etherblood.a.rules.updates.Trigger;

public class PlayerResultSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final Modifier[] modifiers;
    private final Trigger[] triggers;

    public PlayerResultSystem(EntityData data) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.modifiers = new Modifier[0];
        this.triggers = new Trigger[0];
    }

    @Override
    public boolean isActive() {
        return data.list(core.PLAYER_RESULT_REQUEST).nonEmpty();
    }

    @Override
    public void modify() {
        for (int entity : data.list(core.PLAYER_RESULT_REQUEST)) {
            int result = data.get(entity, core.PLAYER_RESULT_REQUEST);
            for (int i = 0; result > 0 && i < modifiers.length; i++) {
                result = modifiers[i].modify(entity, result);
            }
            data.set(entity, core.PLAYER_RESULT_ACTION, result);
            data.remove(entity, core.PLAYER_RESULT_REQUEST);
        }
    }

    @Override
    public void apply() {
        IntList playerResultActions = data.list(core.PLAYER_RESULT_ACTION);
        for (int player : playerResultActions) {
            int result = data.get(player, core.PLAYER_RESULT_ACTION);
            data.set(player, core.PLAYER_RESULT, result);
        }
        if (playerResultActions.nonEmpty()) {
            //TODO: improve game result logic
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (!data.has(player, core.PLAYER_RESULT)) {
                    data.set(player, core.PLAYER_RESULT_REQUEST, PlayerResult.WIN);
                }
            }
        }
    }

//        updatePlayerResults();
//
//        for (int player : data.list(core.PLAYER_RESULT)) {
//            if (data.has(player, core.ACTIVE_PLAYER_PHASE)) {
//                nextTurn(player);
//                data.remove(player, core.ACTIVE_PLAYER_PHASE);
//                data.remove(player, core.END_PHASE_REQUEST);
//            }
//        }
    @Override
    public void triggerAndClean() {
        for (int player : data.list(core.PLAYER_RESULT_ACTION)) {
            int result = data.get(player, core.PLAYER_RESULT_ACTION);

            if (data.has(player, core.ACTIVE_PLAYER_PHASE)) {
                nextTurn(player);
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
                data.remove(player, core.END_PHASE_REQUEST);
            }

            for (Trigger trigger : triggers) {
                trigger.trigger(player, result);
            }
            data.remove(player, core.PLAYER_RESULT_ACTION);
        }
    }

    private void nextTurn(int player) {
        Integer bestPlayer = SystemsUtil.nextPlayer(data, player);
        if (bestPlayer != null) {
            data.set(bestPlayer, core.START_PHASE_REQUEST, PlayerPhase.BLOCK);
        }
    }

    private void updatePlayerResults() {
        IntList players = data.list(core.PLAYER_INDEX);
        IntList alive = new IntList();
        for (int minion : data.list(core.HERO)) {
            if (!data.has(minion, core.IN_BATTLE_ZONE)) {
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
            if (!data.hasValue(winner, core.PLAYER_RESULT, PlayerResult.WIN)) {
                data.set(winner, core.PLAYER_RESULT_REQUEST, PlayerResult.WIN);
            }
        }
        for (int loser : dead) {
            if (!data.hasValue(loser, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                data.set(loser, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
            }
        }
    }

}
