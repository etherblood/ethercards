package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;

public class EndPhase implements Move {

    @Override
    public int hashCode() {
        return -3;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EndPhase;
    }

    @Override
    public void apply(Game game, int player) {
        if(game.getData().has(player, Components.IN_ATTACK_PHASE)){
            game.endAttackPhase(player);
        } else {
            game.endBlockPhase(player);
        }
    }
}
