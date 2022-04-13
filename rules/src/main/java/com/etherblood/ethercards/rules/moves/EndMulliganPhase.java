package com.etherblood.ethercards.rules.moves;

public record EndMulliganPhase(int player) implements Move {

    @Override
    public Integer getPlayer() {
        return player;
    }

}
