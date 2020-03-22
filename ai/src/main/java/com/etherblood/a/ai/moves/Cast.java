package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class Cast implements Move {

    public final int source, target;

    public Cast(int source, int target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 117 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cast)) {
            return false;
        }
        Cast other = (Cast) obj;
        return source == other.source && target == other.target;
    }

    @Override
    public void apply(Game game, int player) {
        game.cast(player, source, target);
    }
}
