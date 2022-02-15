package com.etherblood.ethercards.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ComponentsBuilder {

    private final List<ComponentMeta> components = new ArrayList<>();
    private final List<ComponentsModule> modules = new ArrayList<>();

    public int registerIfAbsent(String name) {
        for (ComponentMeta component : components) {
            if (component.name.equals(name)) {
                return component.id;
            }
        }
        return register(name);
    }

    public int register(String name) {
        int id = components.size();
        components.add(new ComponentMeta(id, name));
        return id;
    }

    public void registerModule(Function<ToIntFunction<String>, ComponentsModule> moduleConstructor) {
        modules.add(moduleConstructor.apply(this::register));
    }

    public Components build() {
        return new Components(components, modules);
    }
}
