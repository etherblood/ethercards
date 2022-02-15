package com.etherblood.ethercards.ai.bots.mcts;

import com.etherblood.ethercards.ai.BotGame;
import com.etherblood.ethercards.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.ethercards.ai.bots.evaluation.SimpleTeamEvaluation;
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
    public Function<Game, float[]> evaluation = new RolloutToEvaluation<>(new Random(), 10, new SimpleTeamEvaluation<Move, Game>()::evaluate)::evaluate;
    public TerminationType termination = TerminationType.NODE_COUNT;
}
