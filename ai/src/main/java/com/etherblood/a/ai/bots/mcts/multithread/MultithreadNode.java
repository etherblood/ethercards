package com.etherblood.a.ai.bots.mcts.multithread;

import com.etherblood.a.rules.moves.Move;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MultithreadNode {

    private static final Object[] EMPTY = new Object[0];

    private final float[] scores;
    private Object[] childs = EMPTY;

    public MultithreadNode(int playerCount) {
        this.scores = new float[playerCount];
    }

    public synchronized float visits() {
        float sum = 0;
        for (float score : scores) {
            sum += score;
        }
        return sum;
    }

    public synchronized float[] getScores() {
        return Arrays.copyOf(scores, scores.length);
    }

    public synchronized float score(int playerIndex) {
        return scores[playerIndex];
    }

    public synchronized void updateScores(float[] playerScores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += playerScores[i];
        }
    }

    public synchronized MultithreadNode getChild(Move move) {
        return Objects.requireNonNull(getChildOrDefault(move, null));
    }

    @SuppressWarnings("unchecked")
    public synchronized MultithreadNode getChildOrDefault(Move move, MultithreadNode defaultValue) {
        for (int i = 0; i < childs.length; i += 2) {
            if (childs[i].equals(move)) {
                return (MultithreadNode) childs[i + 1];
            }
        }
        return defaultValue;
    }

    public synchronized void addChild(Move move, MultithreadNode node) {
        int index = childs.length;
        childs = Arrays.copyOf(childs, childs.length + 2);
        childs[index] = move;
        childs[index + 1] = node;
    }

    @SuppressWarnings("unchecked")
    public synchronized List<Move> getMoves() {
        List<Move> result = new ArrayList<>();
        for (int i = 0; i < childs.length; i += 2) {
            result.add((Move) childs[i]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public synchronized List<MultithreadNode> getChilds() {
        List<MultithreadNode> result = new ArrayList<>();
        for (int i = 1; i < childs.length; i += 2) {
            result.add((MultithreadNode) childs[i]);
        }
        return result;
    }
}
