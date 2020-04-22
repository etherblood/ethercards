package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class Surrender implements Move {

    public final int player;

    public Surrender(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return 157 * player;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Surrender)) {
            return false;
        }
        Surrender other = (Surrender) obj;
        return player == other.player;
    }

    @Override
    public void apply(Game game) {
        game.getMoves().surrender(player);
    }

}
