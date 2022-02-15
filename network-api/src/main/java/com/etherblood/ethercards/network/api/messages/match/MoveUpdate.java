package com.etherblood.ethercards.network.api.messages.match;

import com.etherblood.ethercards.rules.MoveReplay;

public class MoveUpdate {

    public MoveReplay move;

    public MoveUpdate() {
    }

    public MoveUpdate(MoveReplay move) {
        this.move = move;
    }
}
