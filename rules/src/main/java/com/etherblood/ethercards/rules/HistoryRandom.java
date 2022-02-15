package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.entities.collections.IntList;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

public class HistoryRandom implements IntUnaryOperator {

    private final IntUnaryOperator sourceRandom;
    private final IntList history = new IntList();
    private int next = 0;

    private HistoryRandom(IntUnaryOperator random) {
        this.sourceRandom = random;
    }

    public static HistoryRandom producer(IntUnaryOperator random) {
        return new HistoryRandom(random);
    }

    public static HistoryRandom producer() {
        return producer(new SecureRandom()::nextInt);
    }

    public static HistoryRandom consumer() {
        return new HistoryRandom(null);
    }

    @Override
    public int applyAsInt(int operand) {
        if (operand == 1) {
            // skip when only possible result is '0'
            return 0;
        }
        if (next < history.size()) {
            int result = history.get(next++);
            assert 0 <= result && result < operand;
            return result;
        }
        Objects.requireNonNull(sourceRandom, "History random is set to consumer and can't generate random numbers");
        int result = sourceRandom.applyAsInt(operand);
        history.add(result);
        next++;
        return result;
    }

    public IntList getHistory() {
        return history;
    }

}
