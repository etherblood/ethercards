package com.etherblood.ethercards.network.api.messages.match;

import com.etherblood.ethercards.rules.moves.Move;

public class MoveRequest {

    public Move move;

    public MoveRequest() {
    }

    public MoveRequest(Move move) {
        this.move = move;
    }
}
