package com.etherblood.ethercards.rules.moves;

public record EndBlockPhase(int player) implements Move {

    @Override
    public Integer getPlayer() {
        return player;
    }

}
