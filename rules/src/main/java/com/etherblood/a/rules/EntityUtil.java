package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import java.util.HashMap;
import java.util.Map;

public class EntityUtil {

    public static void copy(EntityData source, EntityData destination) {
        for (int component = 0; component < Components.count(); component++) {
            for (int entity : destination.list(component)) {
                destination.remove(entity, component);
            }
            for (int entity : source.list(component)) {
                destination.set(entity, component, source.get(entity, component));
            }
        }
        ((SimpleEntityData) destination).setNextId(((SimpleEntityData) source).getNextId());
    }

    public static Map<String, Integer> extractEntityComponents(EntityData data, int entity) {
        Map<String, Integer> map = new HashMap<>();
        for (ComponentMeta component : Components.getComponents()) {
            data.getOptional(entity, component.id).ifPresent(value -> map.put(component.name, value));
        }
        return map;
    }
}
