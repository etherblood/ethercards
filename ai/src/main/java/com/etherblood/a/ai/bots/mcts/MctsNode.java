package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.rules.moves.Move;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class MctsNode {

    private static final Object[] EMPTY = new Object[0];

    private float visits = 0;
    private final float[] scores;
    private Object[] childs = EMPTY;

    public MctsNode(int playerCount) {
        this.scores = new float[playerCount];
    }

    public synchronized float visits() {
        return visits;
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
        visits++;
    }

    public synchronized MctsNode getChild(Move move) {
        return Objects.requireNonNull(getChildOrDefault(move, null));
    }

    @SuppressWarnings("unchecked")
    public synchronized MctsNode getChildOrDefault(Move move, MctsNode defaultValue) {
        for (int i = 0; i < childs.length; i += 2) {
            if (childs[i].equals(move)) {
                return (MctsNode) childs[i + 1];
            }
        }
        return defaultValue;
    }

    public synchronized void addChild(Move move, MctsNode node) {
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
    public synchronized List<MctsNode> getChilds() {
        List<MctsNode> result = new ArrayList<>();
        for (int i = 1; i < childs.length; i += 2) {
            result.add((MctsNode) childs[i]);
        }
        return result;
    }
}
