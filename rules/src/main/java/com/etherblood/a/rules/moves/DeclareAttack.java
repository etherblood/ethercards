package com.etherblood.a.rules.moves;

public class DeclareAttack implements Move {

    public final int player, source, target;

    public DeclareAttack(int player, int source, int target) {
        this.player = player;
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 137 * player + 79 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareAttack)) {
            return false;
        }
        DeclareAttack other = (DeclareAttack) obj;
        return player == other.player && source == other.source && target == other.target;
    }

    @Override
    public String toString() {
        return "DeclareAttack{" + "player=" + player + ", source=" + source + ", target=" + target + '}';
    }

}
