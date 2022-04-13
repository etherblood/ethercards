package com.etherblood.ethercards.rules.moves;

public record DeclareAttack(int player, int source, int target) implements Move {

    @Override
    public Integer getPlayer() {
        return player;
    }

}
