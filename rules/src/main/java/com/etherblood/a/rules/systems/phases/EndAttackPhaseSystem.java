package com.etherblood.a.rules.systems.phases;

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
        for (int player : data.list(core.END_PHASE_ACTION)) {
            if (data.get(player, core.END_PHASE_ACTION) != PlayerPhase.ATTACK) {
                continue;
            }
            data.remove(player, core.ACTIVE_PLAYER_PHASE);

            for (int attacker : data.list(core.ATTACKS_TARGET).stream()
                    .filter(x -> data.hasValue(x, core.OWNED_BY, player))
                    .toArray()) {
                int target = data.get(attacker, core.ATTACKS_TARGET);
                data.set(attacker, core.TIRED, 1);
                if (!data.has(target, core.IN_BATTLE_ZONE)) {
                    data.remove(attacker, core.ATTACKS_TARGET);
                    continue;
                }
                data.set(target, core.TIRED, 1);
                data.getOptional(attacker, core.DRAWS_ON_ATTACK).ifPresent(draws -> {
                    int owner = data.get(attacker, core.OWNED_BY);
                    SystemsUtil.increase(data, owner, core.DRAW_CARDS, draws);
                });
                data.getOptional(attacker, core.GIVE_DRAWS_ON_ATTACK).ifPresent(draws -> {
                    int targetOwner = data.get(target, core.OWNED_BY);
                    SystemsUtil.increase(data, targetOwner, core.DRAW_CARDS, draws);
                });
            }
            for (int minion : data.list(core.SUMMONING_SICKNESS)) {
                if (data.hasValue(minion, core.OWNED_BY, player)) {
                    SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.SUMMONING_SICKNESS, 1);
                }
            }

            Integer nextPlayer = SystemsUtil.nextPlayer(data, player);
            if (nextPlayer != null) {
                data.set(nextPlayer, core.START_PHASE_REQUEST, PlayerPhase.BLOCK);
            }
        }
    }
}
