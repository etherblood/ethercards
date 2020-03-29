package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class TargetedBlockCast implements MoveGroup {

    public final int player, target;

    public TargetedBlockCast(int player, int target) {
        this.player = player;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 127 * player + target + 7823;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TargetedBlockCast)) {
            return false;
        }
        TargetedBlockCast other = (TargetedBlockCast) obj;
        return player == other.player && target == other.target;
    }

}
