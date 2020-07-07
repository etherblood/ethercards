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
    public void before() {
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
    public void run() {
        for (int player : data.list(core.PLAYER_RESULT_ACTION)) {
            int result = data.get(player, core.PLAYER_RESULT_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(player, result);
            }
        }
    }

    @Override
    public void after() {
        IntList playerResultActions = data.list(core.PLAYER_RESULT_ACTION);
        for (int player : playerResultActions) {
            int result = data.get(player, core.PLAYER_RESULT_ACTION);
            data.set(player, core.PLAYER_RESULT, result);

            if (data.has(player, core.ACTIVE_PLAYER_PHASE)) {
                nextTurn(player);
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
                data.remove(player, core.END_PHASE_REQUEST);
            }

            data.remove(player, core.PLAYER_RESULT_ACTION);
        }
        if (playerResultActions.nonEmpty()) {
            int wins = 0;
            int losses = 0;
            IntList undecided = new IntList();
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (data.hasValue(player, core.PLAYER_RESULT, PlayerResult.WIN)) {
                    wins++;
                } else if (data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                    losses++;
                } else {
                    undecided.add(player);
                }
            }
            if (wins == 0 && undecided.size() == 1) {
                int player = undecided.get(0);
                data.set(player, core.PLAYER_RESULT_REQUEST, PlayerResult.WIN);
            } else if (wins != 0) {
                for (int player : undecided) {
                    data.set(player, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
                }
            } else {
                // multiple active players without winner, game is still undecided
            }
        }
    }

    private void nextTurn(int player) {
        Integer bestPlayer = SystemsUtil.nextPlayer(data, player);
        if (bestPlayer != null) {
            data.set(bestPlayer, core.START_PHASE_REQUEST, PlayerPhase.BLOCK);
        }
    }

}
