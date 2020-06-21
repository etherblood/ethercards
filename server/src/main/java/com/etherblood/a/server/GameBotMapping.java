package com.etherblood.a.server;

import com.etherblood.a.ai.bots.mcts.multithread.MultithreadMctsBot;
import java.util.UUID;

public class GameBotMapping {

    public final UUID gameId;
    public final int playerIndex;
    public final MultithreadMctsBot bot;

    public GameBotMapping(UUID gameId, int playerIndex, MultithreadMctsBot bot) {
        this.gameId = gameId;
        this.playerIndex = playerIndex;
        this.bot = bot;
    }
}
