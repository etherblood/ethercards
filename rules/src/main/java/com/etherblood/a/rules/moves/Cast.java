package com.etherblood.a.rules.moves;

public class Cast implements Move {

    public final int player, source, target;

    public Cast(int player, int source, int target) {
        this.player = player;
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 131 * player + 117 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cast)) {
            return false;
        }
        Cast other = (Cast) obj;
        return player == other.player && source == other.source && target == other.target;
    }

    @Override
    public String toString() {
        return "Cast{" + "player=" + player + ", source=" + source + ", target=" + target + '}';
    }

    @Override
    public Integer getPlayer() {
        return player;
    }
}
