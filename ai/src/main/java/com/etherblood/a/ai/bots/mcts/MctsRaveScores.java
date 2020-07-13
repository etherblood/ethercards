package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.rules.moves.Move;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MctsRaveScores {

    private final Map<Move, MctsRaveScore> map = new ConcurrentHashMap<>();
    private final MctsRaveScore defaultScore;
    private final int playerCount;

    public MctsRaveScores(int playerCount) {
        this.playerCount = playerCount;
        this.defaultScore = new MctsRaveScore(playerCount);
    }

    public MctsRaveScore getDefaultScore() {
        return defaultScore;
    }

    public void updateScores(Move move, float[] scores) {
        map.computeIfAbsent(move, x -> new MctsRaveScore(playerCount)).updateScores(scores);
    }

    public MctsRaveScore get(Move move) {
        return map.getOrDefault(move, defaultScore);
    }

}
