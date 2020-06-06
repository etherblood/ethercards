package com.etherblood.a.rules.moves;

public class Surrender implements Move {

    public final int player;

    public Surrender(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 157 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Surrender)) {
            return false;
        }
        Surrender other = (Surrender) obj;
        return player == other.player;
    }

}
