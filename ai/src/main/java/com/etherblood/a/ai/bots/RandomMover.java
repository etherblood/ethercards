package com.etherblood.ethercards.ai.bots;

import com.etherblood.ethercards.ai.BotGame;
import com.etherblood.ethercards.rules.moves.Move;
import java.util.List;
import java.util.Random;

public class RandomMover<Game extends BotGame<Move, Game>> implements Bot {

    private final BotGame<Move, Game> game;
    private final Random random;

    public RandomMover(BotGame<Move, Game> game, Random random) {
        this.game = game;
        this.random = random;
    }

    @Override
    public Move findMove(int playerIndex) {
        List<Move> moves = game.generateMoves(playerIndex);
        return moves.get(random.nextInt(moves.size()));
    }
}
