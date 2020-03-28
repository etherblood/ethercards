package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class EndAttackPhase implements Move {

    public final int player;

    public EndAttackPhase(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 151 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndAttackPhase)) {
            return false;
        }
        EndAttackPhase other = (EndAttackPhase) obj;
        return player == other.player;
    }

    @Override
    public void apply(Game game) {
        game.endAttackPhase(player);
    }

}
