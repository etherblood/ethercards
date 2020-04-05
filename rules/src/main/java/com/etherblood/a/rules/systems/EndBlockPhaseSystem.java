package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class EndBlockPhaseSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        for (int player : data.list(Components.END_BLOCK_PHASE)) {
            data.remove(player, Components.END_BLOCK_PHASE);
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

            data.set(player, Components.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE);

            int mana = 0;
            int draws = data.getOptional(player, Components.DRAW_CARDS).orElse(0);
            draws++;
            for (int entity : data.list(Components.IN_BATTLE_ZONE).stream()
                    .filter(x -> data.hasValue(x, Components.OWNED_BY, player))
                    .toArray()) {
                data.getOptional(entity, Components.TIRED).ifPresent(tiredness -> {
                    tiredness--;
                    if (tiredness > 0) {
                        data.set(entity, Components.TIRED, tiredness);
                    } else {
                        data.remove(entity, Components.TIRED);
                    }
                });
                data.getOptional(entity, Components.MANA_GROWTH).ifPresent(growth -> {
                    int manaPool = data.getOptional(entity, Components.MANA_POOL).orElse(0);
                    manaPool += growth;
                    data.set(entity, Components.MANA_POOL, manaPool);
                });
                mana += data.getOptional(entity, Components.MANA_POOL).orElse(0);
                draws += data.getOptional(entity, Components.DRAWS_PER_TURN).orElse(0);
            }
            mana = Math.max(mana, 0);
            data.set(player, Components.MANA, mana);

            draws = Math.max(draws, 0);
            data.set(player, Components.DRAW_CARDS, draws);
        }
    }
}
