package com.etherblood.a.rules;

import com.etherblood.a.entities.collections.IntList;
import java.security.SecureRandom;
import java.util.function.IntUnaryOperator;

public class HistoryRandom implements IntUnaryOperator {

    private IntUnaryOperator sourceRandom;
    private final IntList history = new IntList();
    private int next = 0;

    private HistoryRandom(IntUnaryOperator random) {
        this.sourceRandom = random;
    }

    public static HistoryRandom producer() {
        return new HistoryRandom(new SecureRandom()::nextInt);
    }

    public static HistoryRandom consumer() {
        return new HistoryRandom(null);
    }

    @Override
    public int applyAsInt(int operand) {
        if (next < history.size()) {
            int result = history.get(next++);
            assert 0 <= result && result < operand;
            return result;
        }
        int result = sourceRandom.applyAsInt(operand);
        history.add(result);
        next++;
        return result;
    }

    public void convertToProducer() {
        sourceRandom = new SecureRandom()::nextInt;
    }

    public void convertToConsumer() {
        sourceRandom = null;
    }

    public IntList getHistory() {
        return history;
    }

}
