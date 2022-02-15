package com.etherblood.ethercards.network.api.messages.match;

import com.etherblood.ethercards.network.api.GameReplay;

public class ConnectedToMatchUpdate {

    public GameReplay replay;
    public int playerIndex;

    public ConnectedToMatchUpdate(GameReplay replay, int playerIndex) {
        this.replay = replay;
        this.playerIndex = playerIndex;
    }

    ConnectedToMatchUpdate() {
    }
}
