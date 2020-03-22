package com.etherblood.a.entities;

import com.etherblood.a.entities.collections.IntList;
import java.util.OptionalInt;

/**
 *
 * @author Philipp
 */
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
}
