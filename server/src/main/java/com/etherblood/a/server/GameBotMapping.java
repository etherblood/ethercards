package com.etherblood.a.server;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.rules.moves.Move;
import java.util.UUID;

public class GameBotMapping {

    public final UUID gameId;
    public final int playerIndex;
    public final MctsBot<Move, MoveBotGame> bot;

    public GameBotMapping(UUID gameId, int playerIndex, MctsBot<Move, MoveBotGame> bot) {
        this.gameId = gameId;
        this.playerIndex = playerIndex;
        this.bot = bot;
    }
}
