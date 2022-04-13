package com.etherblood.ethercards.rules.moves;

public record EndAttackPhase(int player) implements Move {

    @Override
    public Integer getPlayer() {
        return player;
    }

}
