package com.etherblood.a.rules.moves;

public class DeclareBlock implements Move {

    public final int player, source, target;

    public DeclareBlock(int player, int source, int target) {
        this.player = player;
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareBlock)) {
            return false;
        }
        DeclareBlock other = (DeclareBlock) obj;
        return player == other.player && source == other.source && target == other.target;
    }

    @Override
    public String toString() {
        return "DeclareBlock{" + "player=" + player + ", source=" + source + ", target=" + target + '}';
    }

}
