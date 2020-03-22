package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class Block implements Move {

    public final int source, target;

    public Block(int source, int target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 31 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Block)) {
            return false;
        }
        Block other = (Block) obj;
        return source == other.source && target == other.target;
    }

    @Override
    public void apply(Game game, int player) {
        game.block(player, source, target);
    }

}
