package com.etherblood.a.network.api.messages.matchmaking;

import com.etherblood.a.templates.api.setup.RawGameSetup;

public class GameStarted {

    public int playerIndex;
    public RawGameSetup setup;

    public GameStarted(int playerIndex, RawGameSetup setup) {
        this.playerIndex = playerIndex;
        this.setup = setup;
    }

    GameStarted() {
    }
}
