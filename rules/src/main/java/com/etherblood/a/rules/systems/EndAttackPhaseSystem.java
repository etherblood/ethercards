package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class EndAttackPhaseSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.END_PHASE)) {
            data.remove(player, core.END_PHASE);
            data.remove(player, core.ACTIVE_PLAYER_PHASE);

            for (int attacker : data.list(core.ATTACKS_TARGET).stream()
                    .filter(x -> data.hasValue(x, core.OWNED_BY, player))
                    .toArray()) {
                data.set(attacker, core.TIRED, 1);
                data.getOptional(attacker, core.DRAWS_ON_ATTACK).ifPresent(draws -> {
                    int owner = data.get(attacker, core.OWNED_BY);
                    SystemsUtil.increase(data, owner, core.DRAW_CARDS, draws);
                });
            }

            Integer nextPlayer = SystemsUtil.nextPlayer(data, player);
            if (nextPlayer != null) {
                data.set(nextPlayer, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE);
            }
        }
        for (int minion : data.list(core.SUMMONING_SICKNESS)) {
            SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.SUMMONING_SICKNESS, 1);
        }
    }
}
