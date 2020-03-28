package com.etherblood.a.ai.bots.mcts;

public class RaveScore {

    public final float[] scores;

    public RaveScore(int playerCount) {
        scores = new float[playerCount];
    }

    public void updateScores(float[] playerScores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += playerScores[i];
        }
    }

    public float getScore(int playerIndex) {
        float sum = 0;
        for (float score : scores) {
            sum += score;
        }
        return scores[playerIndex] / sum;
    }
}
