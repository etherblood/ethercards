package com.etherblood.a.entities.collections;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

public class IntMap implements Iterable<Integer> {

    private static final long VALUE_MASK = 0xffffffff00000000L;
    private static final int FREE_KEY = 0;
    private static final long FREE_KEYVALUE = dataKey(FREE_KEY);

    private long[] data;
    private int mask;
    private int freeValue;
    private int count;
    private int fillLimit;
    private final float fillFactor;
    private boolean hasFreeKey;

    public IntMap() {
        this(8, 0.75f);
    }

    public IntMap(int capacity, float fillFactor) {
        this.fillFactor = fillFactor;
        this.mask = capacity - 1;
        assert mask != 0;
        assert (mask & VALUE_MASK) == 0;
        assert (mask & capacity) == 0;
        data = new long[capacity];
        updateFillLimit(capacity);
    }

    void foreach(IntIntConsumer consumer) {
        if (hasFreeKey) {
            consumer.accept(FREE_KEY, freeValue);
        }
        for (long keyValue : data) {
            if (keyValue != FREE_KEYVALUE) {
                consumer.accept(key(keyValue), value(keyValue));
            }
        }
    }

    public void foreachKey(IntConsumer consumer) {
        if (hasFreeKey) {
            consumer.accept(FREE_KEY);
        }
        for (long keyValue : data) {
            if (keyValue != FREE_KEYVALUE) {
                consumer.accept(key(keyValue));
            }
        }
    }

    public boolean hasKey(int key) {
        if (key == FREE_KEY) {
            return hasFreeKey;
        }
        int index = key & mask;
        while (true) {
            long keyValue = data[index];
            if (keyValue == FREE_KEYVALUE) {
                return false;
            }
            if (key(keyValue) == key) {
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
            long keyValue = data[index];
            int keyCandidate = key(keyValue);
            if (keyCandidate == key) {
                return value(keyValue);
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
            long keyValue = data[index];
            if (keyValue == FREE_KEYVALUE) {
                return defaultValue;
            }
            if (key(keyValue) == key) {
                return value(keyValue);
            }
            index = (index + 1) & mask;
        }
    }

    public void set(int key, int value) {
        assert count < data.length;
        if (key == FREE_KEY) {
            if (!hasFreeKey) {
                freeValue = value;
                hasFreeKey = true;
                count++;
            }
            return;
        }
        if (count >= fillLimit) {
            resize(2 * capacity());
        }
        if (uncheckedSet(key, dataValue(value))) {
            count++;
        }
    }

    private boolean uncheckedSet(int key, long shiftedValue) {
        assert key != FREE_KEY;
        int index = key & mask;
        while (true) {
            long keyValue = data[index];
            if (keyValue == FREE_KEYVALUE) {
                data[index] = dataKey(key) | shiftedValue;
                return true;
            }
            if (key(keyValue) == key) {
                data[index] = dataKey(key) | shiftedValue;
                return false;
            }
            index = (index + 1) & mask;
        }
    }

    private void resize(int capacity) {
        assert count < capacity;
        mask = capacity - 1;
        assert (mask & capacity) == 0;
        long[] oldData = data;
        data = new long[capacity];
        Arrays.fill(data, FREE_KEY);
        updateFillLimit(capacity);
        for (long keyValue : oldData) {
            int key = key(keyValue);
            if (key == FREE_KEY) {
                continue;
            }
            uncheckedSet(key, keyValue & VALUE_MASK);
        }
    }

    private void updateFillLimit(int capacity) {
        fillLimit = (int) (fillFactor * capacity) - 1;
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
            long keyValue = data[index];
            if (keyValue == FREE_KEYVALUE) {
                return;
            }
            if (key(keyValue) == key) {
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
            long keyValue = data[pos];
            if (keyValue == FREE_KEYVALUE) {
                data[last] = FREE_KEYVALUE;
                return;
            }
            int key = key(keyValue);
            int slot = key & mask;
            if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                data[last] = dataKey(key) | (keyValue & VALUE_MASK);
                last = pos;
            }
        }
    }

    public int size() {
        return count;
    }

    int capacity() {
        return data.length;
    }

    public void clear() {
        count = 0;
        hasFreeKey = false;
        Arrays.fill(data, FREE_KEYVALUE);
    }

    private static int key(long keyValue) {
        return (int) keyValue;
    }

    private static int value(long keyValue) {
        return (int) (keyValue >>> 32);
    }

    private static long dataValue(int value) {
        return ((long) value) << 32;
    }

    private static long dataKey(int key) {
        return Integer.toUnsignedLong(key);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean isFirst = true;
        IntKeyValueIterator iterator = keyValueIterator();
        while (iterator.next()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append(iterator.key());
            builder.append("->");
            builder.append(iterator.value());
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

    IntKeyValueIterator keyValueIterator() {
        return new IntKeyValueIterator() {
            boolean useFreeKey = hasFreeKey;
            private int i = -1;
            private int key, value;

            @Override
            public boolean next() {
                if (useFreeKey) {
                    key = FREE_KEY;
                    value = freeValue;
                    useFreeKey = false;
                    return true;
                }
                while (++i < data.length) {
                    long keyValue = data[i];
                    if (keyValue != FREE_KEYVALUE) {
                        key = IntMap.key(keyValue);
                        value = IntMap.value(keyValue);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int key() {
                return key;
            }

            @Override
            public int value() {
                return value;
            }
        };
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
        return new PrimitiveIterator.OfInt() {
            private final IntKeyValueIterator keyValueIterator;
            private Integer nextKey;

            {
                keyValueIterator = keyValueIterator();
                updateNext();
            }

            @Override
            public int nextInt() {
                try {
                    return nextKey;
                } finally {
                    updateNext();
                }
            }

            @Override
            public boolean hasNext() {
                return nextKey != null;
            }

            private void updateNext() {
                if (keyValueIterator.next()) {
                    nextKey = keyValueIterator.key();
                } else {
                    nextKey = null;
                }
            }
        };
    }

    public PrimitiveIterator.OfLong packedKeyValueIterator() {
        return new PrimitiveIterator.OfLong() {
            int remaining = count;
            int index = -1;

            @Override
            public long nextLong() {
                assert remaining > 0;
                remaining--;
                if (hasFreeKey) {
                    return dataValue(freeValue) | dataKey(FREE_KEY);
                }
                long keyValue;
                do {
                    index++;
                } while ((keyValue = data[index]) == FREE_KEYVALUE);
                return keyValue;
            }

            @Override
            public boolean hasNext() {
                return remaining != 0;
            }
        };
    }

    public void copyFrom(IntMap other) {
        if (data.length < other.data.length) {
            resize(other.data.length);
        }
        if (data.length == other.data.length) {
            System.arraycopy(other.data, 0, data, 0, data.length);
            hasFreeKey = other.hasFreeKey;
            freeValue = other.freeValue;
            count = other.count;
        } else {
            clear();
            IntKeyValueIterator iterator = other.keyValueIterator();
            while (iterator.next()) {
                set(iterator.key(), iterator.value());
            }
        }
    }

}
