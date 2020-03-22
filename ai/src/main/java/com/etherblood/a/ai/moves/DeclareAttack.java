package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class DeclareAttack implements Move {

    public final int source, target;

    public DeclareAttack(int source, int target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 79 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeclareAttack)) {
            return false;
        }
        DeclareAttack other = (DeclareAttack) obj;
        return source == other.source && target == other.target;
    }

    @Override
    public void apply(Game game, int player) {
        game.declareAttack(player, source, target);
    }

}
