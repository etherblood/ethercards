package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;

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
    }
}
