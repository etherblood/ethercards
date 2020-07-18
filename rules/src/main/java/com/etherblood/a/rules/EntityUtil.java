package com.etherblood.a.rules;

import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityUtil {

    public static void copy(EntityData source, EntityData destination) {
        if (source == destination) {
            return;
        }
        if (destination.getComponents() != source.getComponents()) {
            throw new IllegalArgumentException();
        }
        for (int component = 0; component < source.getComponents().size(); component++) {
            for (int entity : destination.list(component)) {
                destination.remove(entity, component);
            }
            for (int entity : source.list(component)) {
                destination.set(entity, component, source.get(entity, component));
            }
        }
        ((SimpleEntityData) destination).setNextId(((SimpleEntityData) source).getNextId());
    }

    public static Map<Integer, Map<String, Integer>> toMap(EntityData data) {
        List<ComponentMeta> orderedComponents = data.getComponents().getMetas().stream().sorted(Comparator.comparing(x -> x.name)).collect(Collectors.toList());
        List<Integer> orderedEntities = new ArrayList<>();
        for (ComponentMeta meta : data.getComponents().getMetas()) {
            for (Integer entity : data.list(meta.id)) {
                if (!orderedEntities.contains(entity)) {
                    orderedEntities.add(entity);
                }
            }
        }
        orderedEntities.sort(Comparator.naturalOrder());

        Map<Integer, Map<String, Integer>> result = new LinkedHashMap<>();
        for (Integer entity : orderedEntities) {
            result.put(entity, new LinkedHashMap<>());
            for (ComponentMeta component : orderedComponents) {
                data.getOptional(entity, component.id).ifPresent(x -> result.get(entity).put(component.name, x));
            }
        }
        return result;
    }

    public static Map<String, Integer> extractEntityComponents(EntityData data, int entity) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ComponentMeta component : data.getComponents().getMetas()) {
            data.getOptional(entity, component.id).ifPresent(value -> map.put(component.name, value));
        }
        return map;
    }
}
