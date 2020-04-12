package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class UpkeepSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.END_BLOCK_PHASE)) {
            data.remove(player, core.END_BLOCK_PHASE);
            data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE);

            int mana = 0;
            int draws = data.getOptional(player, core.DRAW_CARDS).orElse(0);
            draws++;
            for (int entity : data.list(core.IN_BATTLE_ZONE).stream()
                    .filter(x -> data.hasValue(x, core.OWNED_BY, player))
                    .toArray()) {
                data.getOptional(entity, core.TIRED).ifPresent(tiredness -> {
                    SystemsUtil.decreaseAndRemoveLtZero(data, entity, core.TIRED, 1);
                });
                data.getOptional(entity, core.MANA_GROWTH).ifPresent(growth -> {
                    SystemsUtil.increase(data, entity, core.MANA_POOL, growth);
                });
                mana += data.getOptional(entity, core.MANA_POOL).orElse(0);
                draws += data.getOptional(entity, core.DRAWS_PER_TURN).orElse(0);
            }
            mana = Math.max(mana, 0);
            data.set(player, core.MANA, mana);

            draws = Math.max(draws, 0);
            data.set(player, core.DRAW_CARDS, draws);
        }
    }
}
