package com.etherblood.a.rules.moves;

import java.util.Objects;

public class Cast implements Move {

    public final int player, source;
    public final Integer target;

    public Cast(int player, int source) {
        this(player, source, null);
    }

    public Cast(int player, int source, Integer target) {
        this.player = player;
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 131 * player + 117 * source + Objects.hashCode(target);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cast)) {
            return false;
        }
        Cast other = (Cast) obj;
        return player == other.player && source == other.source && Objects.equals(target, other.target);
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
