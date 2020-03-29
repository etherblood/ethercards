package com.etherblood.a.ai.movegroups.moves.blockphase;

import com.etherblood.a.ai.movegroups.MoveGroup;

public class DeclareBlocker implements MoveGroup {

    public final int player, blocker;

    public DeclareBlocker(int player, int attacker) {
        this.player = player;
        this.blocker = attacker;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * blocker + 672489;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareBlocker)) {
            return false;
        }
        DeclareBlocker other = (DeclareBlocker) obj;
        return player == other.player && blocker == other.blocker;
    }
}
