package com.etherblood.a.ai.bots.mcts.multithread;

import com.etherblood.a.rules.moves.Move;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultithreadRaveScores {

    private final Map<Move, MultithreadRaveScore> map = new ConcurrentHashMap<>();
    private final MultithreadRaveScore defaultScore;
    private final int playerCount;

    public MultithreadRaveScores(int playerCount) {
        this.playerCount = playerCount;
        this.defaultScore = new MultithreadRaveScore(playerCount);
    }

    public MultithreadRaveScore getDefaultScore() {
        return defaultScore;
    }

    public void updateScores(Move move, float[] scores) {
        map.computeIfAbsent(move, x -> new MultithreadRaveScore(playerCount)).updateScores(scores);
//        defaultScore.updateScores(scores);
    }

    public MultithreadRaveScore get(Move move) {
        return map.getOrDefault(move, defaultScore);
    }

}
