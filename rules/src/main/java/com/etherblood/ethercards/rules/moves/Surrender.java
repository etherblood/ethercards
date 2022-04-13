package com.etherblood.ethercards.rules.moves;

public record Surrender(int player) implements Move {

    @Override
    public Integer getPlayer() {
        return player;
    }

}
