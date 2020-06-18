package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class EndBlockPhaseBattleSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.END_PHASE)) {
            if (data.get(player, core.END_PHASE) != PlayerPhase.BLOCK_PHASE) {
                continue;
            }
            for (int attacker : data.list(core.ATTACKS_TARGET)) {
                int attackTarget = data.get(attacker, core.ATTACKS_TARGET);
                if (data.hasValue(attackTarget, core.OWNED_BY, player)) {
                    if (data.has(attackTarget, core.IN_BATTLE_ZONE)) {
                        SystemsUtil.fight(data, attacker, attackTarget);

                        data.getOptional(attackTarget, core.DRAWS_ON_ATTACKED).ifPresent(draws -> {
                            int owner = data.get(attackTarget, core.OWNED_BY);
                            SystemsUtil.increase(data, owner, core.DRAW_CARDS, draws);
                        });
                    }
                    data.remove(attacker, core.ATTACKS_TARGET);
                }
            }
        }
    }
}
