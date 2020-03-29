package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class DeclareTargetedAttackCast implements MoveGroup {

    public final int player, card;

    public DeclareTargetedAttackCast(int player, int card) {
        this.player = player;
        this.card = card;
    }

    @Override
    public int hashCode() {
        return player + 31 * card + 18;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareTargetedAttackCast)) {
            return false;
        }
        DeclareTargetedAttackCast other = (DeclareTargetedAttackCast) obj;
        return player == other.player && card == other.card;
    }

}
