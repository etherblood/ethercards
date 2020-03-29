package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class EndAttackCasting implements MoveGroup {

    public final int player;

    public EndAttackCasting(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 151 * player + 253;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndAttackCasting)) {
            return false;
        }
        EndAttackCasting other = (EndAttackCasting) obj;
        return player == other.player;
    }

}
