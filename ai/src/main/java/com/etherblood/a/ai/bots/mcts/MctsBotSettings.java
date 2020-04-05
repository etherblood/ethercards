package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.BotGame;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class MctsBotSettings<Move, Game extends BotGame<Move, Game>> {

    public boolean verbose = false;
    public int strength = 10_000;
    public float uctConstant = 2;
    public float raveMultiplier = 1;
    public float firstPlayUrgency = 10;
    public Random random = new SecureRandom();
    public Function<Game, float[]> evaluation = this::evaluate;
    
    private float[] evaluate(Game game) {
        while (!game.isGameOver()) {
            List<Move> moves = game.generateMoves();
            Move move = moves.get(random.nextInt(moves.size()));
            game.applyMove(move);
        }
        return game.resultPlayerScores();
    }
}
