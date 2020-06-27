package com.etherblood.a.rules.updates.systems.phases;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class StartBattlePhaseSystem {

    public void run(EntityData data, IntUnaryOperator random, GameEventListener events) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.START_PHASE_ACTION)) {
            if (data.get(player, core.START_PHASE_ACTION) != PlayerPhase.BATTLE) {
                continue;
            }

            for (int blocker : data.list(core.BLOCKS_ATTACKER)) {
                int owner = data.get(blocker, core.OWNED_BY);
                if (owner != player) {
                    continue;
                }
                int attacker = data.get(blocker, core.BLOCKS_ATTACKER);
                SystemsUtil.fight(data, random, attacker, blocker, events);
                if (!data.has(attacker, core.TRAMPLE)) {
                    data.remove(attacker, core.ATTACKS_TARGET);
                }
                data.remove(blocker, core.BLOCKS_ATTACKER);
                SystemsUtil.increase(data, blocker, core.TIRED, 1);

                data.getOptional(blocker, core.DRAWS_ON_BLOCK).ifPresent(draws -> {
                    SystemsUtil.increase(data, owner, core.DRAW_CARDS_REQUEST, draws);
                });
            }
            data.set(player, core.END_PHASE_REQUEST, PlayerPhase.BATTLE);
        }
    }
}
