package com.etherblood.a.rules.updates.systems.phases;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class EndMulliganPhaseSystem {

    public void run(EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        boolean playerEndedMulligan = false;
        for (int player : data.list(core.END_PHASE_ACTION)) {
            if (data.get(player, core.END_PHASE_ACTION) != PlayerPhase.MULLIGAN) {
                continue;
            }
            data.remove(player, core.ACTIVE_PLAYER_PHASE);
            playerEndedMulligan = true;
        }
        if (!playerEndedMulligan) {
            return;
        }
        IntList activePlayers = data.list(core.ACTIVE_PLAYER_PHASE);
        if (activePlayers.isEmpty()) {
            IntMap draws = new IntMap();
            for (int card : data.list(core.MULLIGAN)) {
                int owner = data.get(card, core.OWNED_BY);
                draws.set(owner, draws.getOrElse(owner, 0) + 1);
            }
            for (int player : draws) {
                SystemsUtil.drawCards(data, draws.get(player), random, player);
            }
            for (int card : data.list(core.MULLIGAN)) {
                data.remove(card, core.MULLIGAN);
                data.remove(card, core.IN_HAND_ZONE);
                data.set(card, core.IN_LIBRARY_ZONE, 1);
            }

            Integer startingPlayer = null;
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (data.hasValue(player, core.PLAYER_INDEX, 0)) {
                    startingPlayer = player;
                    break;
                }
            }
            data.set(startingPlayer, core.START_PHASE_REQUEST, PlayerPhase.ATTACK);
        }
    }
}
