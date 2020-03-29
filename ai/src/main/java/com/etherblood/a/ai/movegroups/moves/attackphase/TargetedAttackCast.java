package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class TargetedAttackCast implements MoveGroup {

    public final int player, target;

    public TargetedAttackCast(int player, int target) {
        this.player = player;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * +target + 67839;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TargetedAttackCast)) {
            return false;
        }
        TargetedAttackCast other = (TargetedAttackCast) obj;
        return player == other.player && target == other.target;
    }

}
