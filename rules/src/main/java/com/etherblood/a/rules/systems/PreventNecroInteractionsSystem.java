package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class PreventNecroInteractionsSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int minion : data.list(core.DEATH_REQUEST)) {
            data.remove(minion, core.DAMAGE_REQUEST);
        }
        for (int player : data.list(core.PLAYER_RESULT_REQUEST)) {
            data.remove(player, core.START_PHASE_REQUEST);
            data.remove(player, core.END_PHASE_REQUEST);
        }
    }
}
