package com.etherblood.ethercards.entities;

import com.etherblood.ethercards.entities.collections.IntList;
import java.util.OptionalInt;

public interface EntityData {

    int createEntity();

    boolean has(int entity, int component);

    int get(int entity, int component);

    default boolean hasValue(int entity, int component, int value) {
        return has(entity, component) && get(entity, component) == value;
    }

    default OptionalInt getOptional(int entity, int component) {
        return has(entity, component) ? OptionalInt.of(get(entity, component)) : OptionalInt.empty();
    }

    void set(int entity, int component, int value);

    void remove(int entity, int component);

    IntList list(int component);

    IntList listOrdered(int component, int orderComponent);

    IntList listInValueOrder(int component);

    Components getComponents();

    void clear(int component);

    default IntList findByValue(int component, int value) {
        IntList result = new IntList();
        for (int entity : list(component)) {
            if (hasValue(entity, component, value)) {
                result.add(entity);
            }
        }
        return result;
    }
}
