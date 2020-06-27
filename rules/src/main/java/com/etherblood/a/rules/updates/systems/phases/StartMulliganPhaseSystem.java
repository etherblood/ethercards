package com.etherblood.a.rules.updates.systems.phases;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerPhase;

public class StartMulliganPhaseSystem {

    public void run(EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.START_PHASE_ACTION)) {
            if (data.get(player, core.START_PHASE_ACTION) != PlayerPhase.MULLIGAN) {
                continue;
            }
            data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN);
        }
    }

}
