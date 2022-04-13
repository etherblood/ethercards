package com.etherblood.ethercards.rules.moves;

public record DeclareMulligan(int player, int card) implements Move {

    @Override
    public Integer getPlayer() {
        return player;
    }

}
