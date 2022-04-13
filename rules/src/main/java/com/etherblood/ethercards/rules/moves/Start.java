package com.etherblood.ethercards.rules.moves;

public record Start() implements Move {

    @Override
    public Integer getPlayer() {
        return null;
    }

}
