package com.etherblood.a.ai.movegroups.moves.attackphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class DeclareAttacker implements MoveGroup {

    public final int player, attacker;

    public DeclareAttacker(int player, int attacker) {
        this.player = player;
        this.attacker = attacker;
    }

    @Override
    public int hashCode() {
        return 127 * player + attacker + 37;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareAttacker)) {
            return false;
        }
        DeclareAttacker other = (DeclareAttacker) obj;
        return player == other.player && attacker == other.attacker;
    }
}
