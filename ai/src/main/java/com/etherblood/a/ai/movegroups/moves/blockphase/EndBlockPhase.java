package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class EndBlockPhase implements MoveGroup {

    public final int player;

    public EndBlockPhase(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 151 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndBlockPhase)) {
            return false;
        }
        EndBlockPhase other = (EndBlockPhase) obj;
        return player == other.player;
    }

}
