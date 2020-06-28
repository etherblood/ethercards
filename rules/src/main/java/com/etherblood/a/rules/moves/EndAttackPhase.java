package com.etherblood.a.rules.moves;

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
    public String toString() {
        return "EndAttackPhase{" + "player=" + player + '}';
    }

    @Override
    public Integer getPlayer() {
        return player;
    }

}
