package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class UntargetedAttackCast implements MoveGroup {

    public final int player, card;

    public UntargetedAttackCast(int player, int card) {
        this.player = player;
        this.card = card;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * card + 5368;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UntargetedAttackCast)) {
            return false;
        }
        UntargetedAttackCast other = (UntargetedAttackCast) obj;
        return player == other.player && card == other.card;
    }

}
