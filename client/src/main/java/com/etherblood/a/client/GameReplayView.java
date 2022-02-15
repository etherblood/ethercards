package com.etherblood.ethercards.client;

import com.etherblood.ethercards.network.api.GameReplayService;

public class GameReplayView {

    public final GameReplayService gameReplay;
    public final int playerIndex;

    public GameReplayView(GameReplayService gameReplay, int playerIndex) {
        this.gameReplay = gameReplay;
        this.playerIndex = playerIndex;
    }
}
