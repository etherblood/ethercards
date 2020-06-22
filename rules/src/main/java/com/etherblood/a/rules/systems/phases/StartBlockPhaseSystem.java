package com.etherblood.a.rules.systems.phases;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import java.util.function.IntUnaryOperator;

public class StartBlockPhaseSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener eventListener) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.START_PHASE_ACTION)) {
            if (data.get(player, core.START_PHASE_ACTION) != PlayerPhase.BLOCK) {
                continue;
            }
            data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK);
        }
    }

}
