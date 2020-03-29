package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class EndBlockCasting implements MoveGroup {

    public final int player;

    public EndBlockCasting(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 151 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndBlockCasting)) {
            return false;
        }
        EndBlockCasting other = (EndBlockCasting) obj;
        return player == other.player;
    }

}
