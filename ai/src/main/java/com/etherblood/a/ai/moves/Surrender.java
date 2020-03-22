package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class Surrender implements Move {

    @Override
    public int hashCode() {
        return 17;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Surrender;
    }

    @Override
    public void apply(Game game, int player) {
        game.surrender(player);
    }
}
