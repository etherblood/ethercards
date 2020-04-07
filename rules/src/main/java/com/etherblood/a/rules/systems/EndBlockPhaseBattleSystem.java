package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class EndBlockPhaseBattleSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        for (int player : data.list(Components.END_BLOCK_PHASE)) {
            for (int attacker : data.list(Components.ATTACKS_TARGET)) {
                int attackTarget = data.get(attacker, Components.ATTACKS_TARGET);
                if (data.hasValue(attackTarget, Components.OWNED_BY, player)) {
                    if (data.has(attackTarget, Components.IN_BATTLE_ZONE)) {
                        SystemsUtil.fight(data, attacker, attackTarget);

                        data.getOptional(attackTarget, Components.DRAWS_ON_ATTACKED).ifPresent(draws -> {
                            int owner = data.get(attackTarget, Components.OWNED_BY);
                            SystemsUtil.increase(data, owner, Components.DRAW_CARDS, draws);
                        });
                    }
                    data.remove(attacker, Components.ATTACKS_TARGET);
                }
            }
        }
    }
}
