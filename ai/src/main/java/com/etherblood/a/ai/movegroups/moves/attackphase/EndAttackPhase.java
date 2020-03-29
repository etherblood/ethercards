package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class EndAttackPhase implements MoveGroup {

    public final int player;

    public EndAttackPhase(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 151 * player + 219;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndAttackPhase)) {
            return false;
        }
        EndAttackPhase other = (EndAttackPhase) obj;
        return player == other.player;
    }

}
