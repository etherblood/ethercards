package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.rules.moves.Move;

public record MoveReplay(
        Move move,
        int[] randomResults
) {

}
