package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.BotGame;
import java.util.List;
import java.util.Random;

public class RandomMover<Move, Game extends BotGame<Move, Game>> {

    private final Random random;

    public RandomMover(Random random) {
        this.random = random;
    }

    public Move nextMove(Game game) {
        List<Move> moves = game.generateMoves();
        return moves.get(random.nextInt(moves.size()));
    }
}
