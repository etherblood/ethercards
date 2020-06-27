package com.etherblood.a.rules.updates.systems.phases;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.updates.SystemsUtil;

public class StartAttackPhaseSystem {

    public void run(EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.START_PHASE_ACTION)) {
            if (data.get(player, core.START_PHASE_ACTION) != PlayerPhase.ATTACK) {
                continue;
            }
            data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK);

            int mana = 0;
            int draws = data.getOptional(player, core.DRAW_CARDS_REQUEST).orElse(0);
            for (int entity : data.list(core.IN_BATTLE_ZONE).stream()
                    .filter(x -> data.hasValue(x, core.OWNED_BY, player))
                    .toArray()) {
                data.getOptional(entity, core.TIRED).ifPresent(tiredness -> {
                    SystemsUtil.setAndRemoveLtZero(data, entity, core.TIRED, tiredness - 1);
                });
                data.getOptional(entity, core.MANA_GROWTH).ifPresent(growth -> {
                    SystemsUtil.increase(data, entity, core.MANA_POOL, growth);
                });
                data.getOptional(entity, core.POISONED).ifPresent(poison -> {
                    SystemsUtil.damage(data, entity, poison);
                    SystemsUtil.decreaseAndRemoveLtZero(data, entity, core.POISONED, 1);
                });
                mana += data.getOptional(entity, core.MANA_POOL).orElse(0);
                draws += data.getOptional(entity, core.DRAWS_PER_TURN).orElse(0);
            }
            mana = Math.max(mana, 0);
            data.set(player, core.MANA, mana);

            draws = Math.max(draws, 0);
            data.set(player, core.DRAW_CARDS_REQUEST, draws);
        }
    }
}
