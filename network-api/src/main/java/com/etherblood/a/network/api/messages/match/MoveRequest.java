package com.etherblood.a.network.api.messages.match;

import com.etherblood.a.rules.moves.Move;

public class MoveRequest {

    public Move move;

    public MoveRequest() {
    }

    public MoveRequest(Move move) {
        this.move = move;
    }
}
