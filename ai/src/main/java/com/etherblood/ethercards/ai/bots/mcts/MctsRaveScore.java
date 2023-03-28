package com.etherblood.ethercards.ai.bots.mcts;

class MctsRaveScore {

    private final float[] scores;

    public MctsRaveScore(int playerCount) {
        scores = new float[playerCount];
    }

    public synchronized void updateScores(float playerScores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += playerScores;
        }
    }

    public synchronized void updateScores(float[] playerScores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += playerScores[i];
        }
    }

    public synchronized float getScore(int playerIndex) {
        float sum = 0;
        for (float score : scores) {
            sum += score;
        }
        if (sum == 0) {
            return 0;
        }
        return scores[playerIndex] / sum;
    }
}
