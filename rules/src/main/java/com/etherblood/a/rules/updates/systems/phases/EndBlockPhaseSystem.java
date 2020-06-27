package com.etherblood.a.rules.updates.systems.phases;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class EndBlockPhaseSystem {

    public void run(EntityData data, IntUnaryOperator random, GameEventListener events) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.END_PHASE_ACTION)) {
            if (data.get(player, core.END_PHASE_ACTION) != PlayerPhase.BLOCK) {
                continue;
            }

//            for (int blocker : data.list(core.BLOCKS_ATTACKER)) {
//                int owner = data.get(blocker, core.OWNED_BY);
//                if (owner != player) {
//                    continue;
//                }
//                int attacker = data.get(blocker, core.BLOCKS_ATTACKER);
//                SystemsUtil.fight(data, random, attacker, blocker, events);
//                if (!data.has(attacker, core.TRAMPLE)) {
//                    data.remove(attacker, core.ATTACKS_TARGET);
//                }
//                data.remove(blocker, core.BLOCKS_ATTACKER);
//                SystemsUtil.increase(data, blocker, core.TIRED, 1);
//
//                data.getOptional(blocker, core.DRAWS_ON_BLOCK).ifPresent(draws -> {
//                    SystemsUtil.increase(data, owner, core.DRAW_CARDS, draws);
//                });
//            }
//
//            for (int attacker : data.list(core.ATTACKS_TARGET)) {
//                int attackTarget = data.get(attacker, core.ATTACKS_TARGET);
//                if (data.hasValue(attackTarget, core.OWNED_BY, player)) {
//                    if (data.has(attackTarget, core.IN_BATTLE_ZONE)) {
//                        SystemsUtil.fight(data, random, attacker, attackTarget, events);
//
//                        data.getOptional(attackTarget, core.DRAWS_ON_ATTACKED).ifPresent(draws -> {
//                            int owner = data.get(attackTarget, core.OWNED_BY);
//                            SystemsUtil.increase(data, owner, core.DRAW_CARDS, draws);
//                        });
//                    }
//                    data.remove(attacker, core.ATTACKS_TARGET);
//                }
//            }
            data.set(player, core.START_PHASE_REQUEST, PlayerPhase.BATTLE);
        }
    }
}
