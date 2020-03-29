package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class Block implements MoveGroup {

    public final int player, target;

    public Block(int player, int target) {
        this.player = player;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 127 * player + target - 379;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Block)) {
            return false;
        }
        Block other = (Block) obj;
        return player == other.player && target == other.target;
    }

}
