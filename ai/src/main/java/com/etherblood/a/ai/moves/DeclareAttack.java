package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

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
    public void apply(Game game) {
        game.getMoves().declareAttack(player, source, target);
    }

}
