package com.etherblood.a.entities;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

/**
 *
 * @author Philipp
 */
public class SimpleEntityData implements EntityData {

    private final IntSupplier idSequence;
    private final IntMap[] map;
    
    public SimpleEntityData(int componentCount) {
        this(componentCount, new AtomicInteger()::incrementAndGet);
    }

    public SimpleEntityData(int componentCount, IntSupplier idSequence) {
        this.idSequence = idSequence;
        this.map = new IntMap[componentCount];
        for (int component = 0; component < componentCount; component++) {
            map[component] = new IntMap();
        }
    }

    @Override
    public int createEntity() {
        return idSequence.getAsInt();
    }

    @Override
    public boolean has(int entity, int component) {
        return component(component).hasKey(entity);
    }

    @Override
    public int get(int entity, int component) {
        return component(component).get(entity);
    }

    @Override
    public void set(int entity, int component, int value) {
        component(component).set(entity, value);
    }

    @Override
    public void remove(int entity, int component) {
        component(component).remove(entity);
    }

    private IntMap component(int component) {
        return map[component];
    }

    @Override
    public OptionalInt getOptional(int entity, int component) {
        return component(component).getOptional(entity);
    }

    @Override
    public boolean hasValue(int entity, int component, int value) {
        return component(component).getOrElse(entity, ~value) == value;
    }

    @Override
    public IntList list(int component) {
        IntList list = new IntList();
        component(component).foreachKey(list::add);
        list.sort();
        return list;
    }
}
