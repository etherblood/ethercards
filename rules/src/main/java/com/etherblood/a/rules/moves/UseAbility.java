package com.etherblood.a.rules.moves;

import java.util.Objects;

public class UseAbility implements Move {

    public final int player, source;
    public final Integer target;

    public UseAbility(int player, int source) {
        this(player, source, null);
    }

    public UseAbility(int player, int source, Integer target) {
        this.player = player;
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 139 * player + 83 * source + Objects.hashCode(target);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UseAbility)) {
            return false;
        }
        UseAbility other = (UseAbility) obj;
        return player == other.player && source == other.source && Objects.equals(target, other.target);
    }

    @Override
    public String toString() {
        return "UseAbility{" + "player=" + player + ", source=" + source + ", target=" + target + '}';
    }

    @Override
    public Integer getPlayer() {
        return player;
    }

}
