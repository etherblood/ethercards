package com.etherblood.ethercards.game.server;

import com.etherblood.ethercards.ai.MoveBotGame;
import com.etherblood.ethercards.ai.bots.Bot;
import java.util.UUID;

public class GameBotMapping {

    public final UUID gameId;
    public final int playerIndex;
    public final MoveBotGame game;
    public final Bot bot;

    public GameBotMapping(UUID gameId, int playerIndex, MoveBotGame game, Bot bot) {
        this.gameId = gameId;
        this.playerIndex = playerIndex;
        this.game = game;
        this.bot = bot;
    }
}
