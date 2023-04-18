package com.etherblood.ethercards.entities.collections;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

public class IntMap implements Iterable<Integer> {

    private static final int FREE_KEY = 0;

    private int[] keys;
    private int[] values;
    private int mask;
    private int freeValue;
    private int count;
    private int fillLimit;
    private final float fillFactor;
    private boolean hasFreeKey;

    public IntMap() {
        this(16, 0.75f);
    }

    public IntMap(int capacity, float fillFactor) {
        if (Integer.bitCount(capacity) != 1) {
            throw new IllegalArgumentException("Capacity must be a power of 2");
        }
        this.fillFactor = fillFactor;
        this.mask = capacity - 1;
        keys = new int[capacity];
        values = new int[capacity];
        updateFillLimit();
    }

    public void foreachKey(IntConsumer consumer) {
        if (hasFreeKey) {
            consumer.accept(FREE_KEY);
        }
        for (int i = 0; i <= mask; i++) {
            int key = keys[i];
            if (key != FREE_KEY) {
                consumer.accept(key);
            }
        }
    }

    public boolean hasKey(int key) {
        if (key == FREE_KEY) {
            return hasFreeKey;
        }
        int index = key & mask;
        while (true) {
            int keyCandidate = keys[index];
            if (keyCandidate == FREE_KEY) {
                return false;
            }
            if (keyCandidate == key) {
                return true;
            }
            index = (index + 1) & mask;
        }
    }

    public int get(int key) throws NoSuchElementException {
        if (key == FREE_KEY) {
            if (hasFreeKey) {
                return freeValue;
            }
            throw new NoSuchElementException();
        }
        int index = key & mask;
        while (true) {
            int keyCandidate = keys[index];
            if (keyCandidate == key) {
                return values[index];
            }
            if (keyCandidate == FREE_KEY) {
                throw new NoSuchElementException();
            }
            index = (index + 1) & mask;
        }
    }

    public int getOrElse(int key, int defaultValue) {
        if (key == FREE_KEY) {
            return hasFreeKey ? freeValue : defaultValue;
        }
        int index = key & mask;
        while (true) {
            int keyCandidate = keys[index];
            if (keyCandidate == FREE_KEY) {
                return defaultValue;
            }
            if (keyCandidate == key) {
                return values[index];
            }
            index = (index + 1) & mask;
        }
    }

    public void set(int key, int value) {
        if (key == FREE_KEY) {
            if (!hasFreeKey) {
                hasFreeKey = true;
                count++;
            }
            freeValue = value;
            return;
        }
        if (count >= fillLimit) {
            resize(2 * capacity());
        }
        if (uncheckedSet(key, value)) {
            count++;
        }
    }

    private boolean uncheckedSet(int key, int value) {
        assert key != FREE_KEY;
        int index = key & mask;
        while (true) {
            int keyCandidate = keys[index];
            if (keyCandidate == FREE_KEY) {
                keys[index] = key;
                values[index] = value;
                return true;
            }
            if (keyCandidate == key) {
                values[index] = value;
                return false;
            }
            index = (index + 1) & mask;
        }
    }

    private void resize(int capacity) {
        assert count < capacity;
        int oldMask = mask;
        int[] oldKeys = keys;
        int[] oldValues = values;
        keys = new int[capacity];
        values = new int[capacity];
        mask = capacity - 1;
        assert (mask & capacity) == 0;
        for (int i = 0; i <= oldMask; i++) {
            int key = oldKeys[i];
            if (key == FREE_KEY) {
                continue;
            }
            uncheckedSet(key, oldValues[i]);
        }
        updateFillLimit();
    }

    private void updateFillLimit() {
        fillLimit = (int) (fillFactor * capacity()) - 1;
    }

    public void remove(int key) {
        if (key == FREE_KEY) {
            if (hasFreeKey) {
                hasFreeKey = false;
                count--;
            }
            return;
        }
        int index = key & mask;
        while (true) {
            int keyCandidate = keys[index];
            if (keyCandidate == FREE_KEY) {
                return;
            }
            if (keyCandidate == key) {
                shift(index);
                count--;
                return;
            }
            index = (index + 1) & mask;
        }
    }

    private void shift(int pos) {
        int last = pos;
        while (true) {
            pos = (pos + 1) & mask;
            int keyCandidate = keys[pos];
            if (keyCandidate == FREE_KEY) {
                keys[last] = FREE_KEY;
                return;
            }
            int slot = keyCandidate & mask;
            if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                keys[last] = keyCandidate;
                values[last] = values[pos];
                last = pos;
            }
        }
    }

    public int size() {
        return count;
    }

    int capacity() {
        return mask + 1;
    }

    public void clear() {
        count = 0;
        hasFreeKey = false;
        Arrays.fill(keys, FREE_KEY);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean isFirst = true;
        for (int key : this) {
            int value = get(key);
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append(key);
            builder.append("->");
            builder.append(value);
        }
        builder.append('}');
        return builder.toString();
    }

    public OptionalInt getOptional(int key) {
        return hasKey(key) ? OptionalInt.of(get(key)) : OptionalInt.empty();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
        return new PrimitiveIterator.OfInt() {
            private int remaining = count;
            private int currentIndex = -1;
            private boolean freeKey = hasFreeKey;

            @Override
            public int nextInt() {
                remaining--;
                if (freeKey) {
                    freeKey = false;
                    return FREE_KEY;
                }
                int result;
                do {
                    currentIndex++;
                } while ((result = keys[currentIndex]) == FREE_KEY);
                return result;
            }

            @Override
            public boolean hasNext() {
                return remaining != 0;
            }
        };
    }

    public void copyFrom(IntMap other) {
        if (this == other) {
            throw new IllegalArgumentException("Cannot copy from itself.");
        }
        if (keys.length < other.capacity()) {
            keys = new int[other.keys.length];
            values = new int[other.keys.length];
        }
        System.arraycopy(other.keys, 0, keys, 0, other.capacity());
        System.arraycopy(other.values, 0, values, 0, other.capacity());
        hasFreeKey = other.hasFreeKey;
        freeValue = other.freeValue;
        count = other.count;
        mask = other.mask;
        updateFillLimit();
    }

}
