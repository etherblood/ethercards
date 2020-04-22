package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class EndMulliganPhase implements Move {

    public final int player;

    public EndMulliganPhase(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 139 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndMulliganPhase)) {
            return false;
        }
        EndMulliganPhase other = (EndMulliganPhase) obj;
        return player == other.player;
    }

    @Override
    public void apply(Game game) {
        game.getMoves().endMulliganPhase(player);
    }

}
