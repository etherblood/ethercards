package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class UntargetedBlockCast implements MoveGroup {

    public final int player, card;

    public UntargetedBlockCast(int player, int card) {
        this.player = player;
        this.card = card;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * card + 89236;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UntargetedBlockCast)) {
            return false;
        }
        UntargetedBlockCast other = (UntargetedBlockCast) obj;
        return player == other.player && card == other.card;
    }

}
