package com.etherblood.a.entities;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import java.util.OptionalInt;

/**
 *
 * @author Philipp
 */
public class SimpleEntityData implements EntityData {

    private int nextId = 1;
    private final IntMap[] map;
    private final Components components;

    public SimpleEntityData(Components components) {
        this.components = components;
        this.map = new IntMap[components.size()];
        for (int component = 0; component < components.size(); component++) {
            map[component] = new IntMap();
        }
    }

    @Override
    public int createEntity() {
        return nextId++;
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
        IntMap componentMaps = component(component);
        if (componentMaps.isEmpty()) {
            return new IntList(0);
        }
        IntList list = new IntList();
        componentMaps.foreachKey(list::add);
        if (list.size() > 1) {
            list.sort();
        }
        return list;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    @Override
    public Components getComponents() {
        return components;
    }
}
