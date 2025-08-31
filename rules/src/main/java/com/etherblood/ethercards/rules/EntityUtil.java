package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.entities.ComponentMeta;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.SimpleEntityData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityUtil {

    public static void copy(EntityData source, EntityData destination) {
        ((SimpleEntityData) destination).copyFromImmutable(((SimpleEntityData) source));
    }

    public static Map<Integer, Map<String, Integer>> toMap(EntityData data) {
        List<ComponentMeta> orderedComponents = data.getSchema().getMetas().stream().sorted(Comparator.comparing(x -> x.name)).collect(Collectors.toList());
        List<Integer> orderedEntities = new ArrayList<>();
        for (ComponentMeta meta : data.getSchema().getMetas()) {
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
        for (ComponentMeta component : data.getSchema().getMetas()) {
            data.getOptional(entity, component.id).ifPresent(value -> map.put(component.name, value));
        }
        return map;
    }
}
