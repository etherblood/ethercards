package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class EndMulliganPhaseSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.END_PHASE)) {
            data.remove(player, core.END_PHASE);
            data.remove(player, core.ACTIVE_PLAYER_PHASE);
        }
        IntList activePlayers = data.list(core.ACTIVE_PLAYER_PHASE);
        if (activePlayers.isEmpty()) {
            for (int card : data.list(core.MULLIGAN)) {
                data.remove(card, core.MULLIGAN);
                data.remove(card, core.IN_HAND_ZONE);
                data.set(card, core.IN_LIBRARY_ZONE, 1);
                int owner = data.get(card, core.OWNED_BY);
                SystemsUtil.increase(data, owner, core.DRAW_CARDS, 1);
            }

            Integer startingPlayer = null;
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (data.get(player, core.PLAYER_INDEX) == 0) {
                    startingPlayer = player;
                    break;
                }
            }
            data.set(startingPlayer, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE);
            data.set(startingPlayer, core.END_PHASE, 1);
        }
    }
}
