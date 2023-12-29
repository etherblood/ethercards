package com.etherblood.ethercards.entities;

import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.entities.collections.IntMap;

import java.util.Arrays;
import java.util.OptionalInt;

public class SimpleEntityData implements EntityData {

    private int nextId = 1;
    private final IntMap[] tables;
    private final Components schema;

    private final EntityList[] listCache;

    public SimpleEntityData(Components schema) {
        this.schema = schema;
        this.tables = new IntMap[schema.size()];
        listCache = new EntityList[schema.size()];
        for (int component = 0; component < schema.size(); component++) {
            tables[component] = new IntMap();
            listCache[component] = EntityList.EMPTY;
        }
    }

    @Override
    public int createEntity() {
        return nextId++;
    }

    @Override
    public boolean has(int entity, int component) {
        return tables[component].hasKey(entity);
    }

    @Override
    public int get(int entity, int component) {
        return tables[component].get(entity);
    }

    @Override
    public void set(int entity, int component, int value) {
        listCache[component] = EntityList.EMPTY;
        tables[component].set(entity, value);
    }

    @Override
    public void remove(int entity, int component) {
        listCache[component] = EntityList.EMPTY;
        tables[component].remove(entity);
    }

    @Override
    public void clear(int component) {
        listCache[component] = EntityList.EMPTY;
        tables[component].clear();
    }

    @Override
    public OptionalInt getOptional(int entity, int component) {
        return tables[component].getOptional(entity);
    }

    @Override
    public boolean hasValue(int entity, int component, int value) {
        return tables[component].getOrElse(entity, ~value) == value;
    }

    @Override
<<<<<<< HEAD
    public IntList list(int component) {
        IntMap componentMap = component(component);
        if (componentMap.isEmpty()) {
            return new IntList(0);
        }
        IntList list = new IntList(componentMap.size());
        componentMap.foreachKey(list::add);
        if (list.size() > 1) {
            list.sort();
        }
        return list;
=======
    public EntityList list(int component) {
        IntMap table = tables[component];
        if (table.size() != listCache[component].size()) {
            IntList list = new IntList();
            table.foreachKey(list::add);
            if (list.size() > 1) {
                list.sort();
            }
            listCache[component] = new EntityList(list);
        }
        return listCache[component];
>>>>>>> f29294a (Slight performance improvement by caching entity lists.)
    }

    @Override
    public EntityList listOrdered(int component, int orderComponent) {
        IntMap componentMap = tables[component];
        if (componentMap.isEmpty()) {
            return EntityList.EMPTY;
        }
        IntMap orderMap = tables[orderComponent];
        long[] entityOrderValues = new long[componentMap.size()];
        int index = 0;
        for (int entity : componentMap) {
            entityOrderValues[index++] = Integer.toUnsignedLong(entity) | ((long) orderMap.get(entity) << 32);
        }
        Arrays.sort(entityOrderValues);
        IntList list = new IntList();
        for (long packedKeyValue : entityOrderValues) {
            list.add((int) packedKeyValue);
        }
        return new EntityList(list);
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    @Override
    public Components getSchema() {
        return schema;
    }

    public void copyFrom(SimpleEntityData other) {
        if (this == other) {
            return;
        }
        if (schema != other.schema) {
            throw new UnsupportedOperationException("Copy requires identical schemas");
        }
        for (int component = 0; component < tables.length; component++) {
            tables[component].copyFrom(other.tables[component]);
            listCache[component] = EntityList.EMPTY;
        }
        nextId = other.nextId;
    }
}
