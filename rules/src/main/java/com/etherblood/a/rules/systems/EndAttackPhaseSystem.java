package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class EndAttackPhaseSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        for (int player : data.list(Components.END_ATTACK_PHASE)) {
            data.remove(player, Components.END_ATTACK_PHASE);
            data.remove(player, Components.ACTIVE_PLAYER_PHASE);

            for (int attacker : data.list(Components.ATTACKS_TARGET).stream()
                    .filter(x -> data.hasValue(x, Components.OWNED_BY, player))
                    .toArray()) {
                data.set(attacker, Components.TIRED, 1);
                data.getOptional(attacker, Components.DRAWS_ON_ATTACK).ifPresent(draws -> {
                    int owner = data.get(attacker, Components.OWNED_BY);
                    SystemsUtil.increase(data, owner, Components.DRAW_CARDS, draws);
                });
            }

            Integer nextPlayer = SystemsUtil.nextPlayer(data, player);
            if (nextPlayer != null) {
                data.set(nextPlayer, Components.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE);
            }
        }
    }
}
