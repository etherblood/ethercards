package com.etherblood.ethercards.ai.bots.evaluation;

import com.etherblood.ethercards.ai.BotGame;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class RolloutToEvaluation<Move, Game extends BotGame<Move, Game>> {

    private final Random random;
    private final int rolloutMoves;
    private final Function<Game, float[]> evaluation;

    public RolloutToEvaluation(Random random, int rolloutMoves, Function<Game, float[]> evaluation) {
        this.random = random;
        this.rolloutMoves = rolloutMoves;
        this.evaluation = evaluation;
    }

    public float[] evaluate(Game game) {
        for (int i = 0; i < rolloutMoves; i++) {
            if (game.isGameOver()) {
                return game.resultPlayerScores();
            }
            List<Move> moves = game.generateMoves();
            Move move = moves.get(random.nextInt(moves.size()));
            game.applyMove(move);
        }
        return evaluation.apply(game);
    }
}
