package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class DeclareAttack implements MoveGroup {

    public final int player, target;

    public DeclareAttack(int player, int target) {
        this.player = player;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 31 * player + target + 345;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareAttack)) {
            return false;
        }
        DeclareAttack other = (DeclareAttack) obj;
        return player == other.player && target == other.target;
    }

}
