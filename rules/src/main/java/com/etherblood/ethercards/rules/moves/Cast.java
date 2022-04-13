package com.etherblood.ethercards.rules.moves;

public record Cast(
        int player,
        int source,
        Integer target
) implements Move {


    public Cast(int player, int source) {
        this(player, source, null);
    }

    @Override
    public Integer getPlayer() {
        return player;
    }
}
