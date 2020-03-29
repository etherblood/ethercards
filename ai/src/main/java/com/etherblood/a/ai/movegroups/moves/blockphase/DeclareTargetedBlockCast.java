package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class DeclareTargetedBlockCast implements MoveGroup {

    public final int player, card;

    public DeclareTargetedBlockCast(int player, int card) {
        this.player = player;
        this.card = card;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * card + 5;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareTargetedBlockCast)) {
            return false;
        }
        DeclareTargetedBlockCast other = (DeclareTargetedBlockCast) obj;
        return player == other.player && card == other.card;
    }

}
