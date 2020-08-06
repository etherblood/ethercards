package com.etherblood.a.network.api.messages.match;

import com.etherblood.a.rules.MoveReplay;

public class MoveUpdate {

    public MoveReplay move;

    public MoveUpdate() {
    }

    public MoveUpdate(MoveReplay move) {
        this.move = move;
    }
}
