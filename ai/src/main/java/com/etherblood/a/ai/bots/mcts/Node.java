package com.etherblood.a.ai.bots.mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Node<T> {

    private static final Object[] EMPTY = new Object[0];

    private final float scores[];
    private Object[] childs = EMPTY;

    public Node(int playerCount) {
        this.scores = new float[playerCount];
    }

    public float visits() {
        float sum = 0;
        for (float score : scores) {
            sum += score;
        }
        return sum;
    }

    public float[] getScores() {
        return scores;
    }

    public float score(int playerIndex) {
        return scores[playerIndex];
    }

    public void updateScores(float[] playerScores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += playerScores[i];
        }
    }

    public Node getChild(T move) {
        return Objects.requireNonNull(getChildOrDefault(move, null));
    }

    public Node getChildOrDefault(T move, Node defaultValue) {
        for (int i = 0; i < childs.length; i += 2) {
            if (childs[i].equals(move)) {
                return (Node) childs[i + 1];
            }
        }
        return defaultValue;
    }

    public void addChild(T move, Node node) {
        int index = childs.length;
        childs = Arrays.copyOf(childs, childs.length + 2);
        childs[index] = move;
        childs[index + 1] = node;
    }

    public List<T> getMoves() {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < childs.length; i += 2) {
            result.add((T) childs[i]);
        }
        return result;
    }
}
