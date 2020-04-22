package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class EndBlockPhase implements Move {

    public final int player;

    public EndBlockPhase(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 149 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndBlockPhase)) {
            return false;
        }
        EndBlockPhase other = (EndBlockPhase) obj;
        return player == other.player;
    }

    @Override
    public void apply(Game game) {
        game.getMoves().endBlockPhase(player);
    }

}
