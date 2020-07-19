package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.a.ai.bots.evaluation.TeamEvaluation;
import java.util.Random;
import java.util.function.Function;

public class MctsBotSettings<Move, Game extends BotGame<Move, Game>> {

    public boolean verbose = false;
    public int maxThreads = 1;
    public int strength = 10_000;
    public float uctConstant = 2;
    public float raveMultiplier = 1;
    public float firstPlayUrgency = 10;
    public Random random = new Random();
    public Function<Game, float[]> evaluation = new RolloutToEvaluation<>(new Random(), 10, new TeamEvaluation<Move, Game>()::evaluate)::evaluate;
}
