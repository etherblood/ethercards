package com.etherblood.a.rules.moves;

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
    public String toString() {
        return "EndMulliganPhase{" + "player=" + player + '}';
    }

}
