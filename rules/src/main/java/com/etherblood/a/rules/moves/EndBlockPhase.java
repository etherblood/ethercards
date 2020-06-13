package com.etherblood.a.rules.moves;

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
    public String toString() {
        return "EndBlockPhase{" + "player=" + player + '}';
    }

}
