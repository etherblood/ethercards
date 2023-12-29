package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.rules.moves.Move;

import java.util.Arrays;

public record MoveReplay(
        Move move,
        int[] rolls
) {

    @Override
    public String toString() {
        return "MoveReplay{"
                + "move=" + move
                + (rolls.length > 0 ? ", rolls=" + Arrays.toString(rolls) : "")
                + '}';
    }
}
