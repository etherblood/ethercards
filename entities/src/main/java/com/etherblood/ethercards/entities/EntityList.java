package com.etherblood.ethercards.entities;

import com.etherblood.ethercards.entities.collections.IntList;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class EntityList implements Iterable<Integer> {

    public static EntityList EMPTY = new EntityList();
    private final IntList list;

    public EntityList(int... data) {
        this(new IntList(data));
    }

    public EntityList(IntList list) {
        this.list = list;
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
        PrimitiveIterator.OfInt iterator = list.iterator();
        return new PrimitiveIterator.OfInt() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public int nextInt() {
                return iterator.nextInt();
            }

            @Override
            public void remove() {
                // this is supposed to be readonly
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public IntStream stream() {
        return list.stream();
    }

    public int size() {
        return list.size();
    }

    public boolean contains(int item) {
        return list.contains(item);
    }

    public boolean nonEmpty() {
        return list.nonEmpty();
    }

    public int get(int index) {
        return list.get(index);
    }
}
