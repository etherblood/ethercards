package com.etherblood.a.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Components {

    private final List<ComponentMeta> components;
    private final Map<Class<? extends ComponentsModule>, ? extends ComponentsModule> modules;

    public Components(List<ComponentMeta> components, List<ComponentsModule> modules) {
        this.components = new ArrayList<>(components);
        this.modules = modules.stream().collect(Collectors.toMap(x -> x.getClass(), x -> x));
    }

    public ComponentMeta getMeta(int id) {
        return components.get(id);
    }

    public <T extends ComponentsModule> T getModule(Class<T> clazz) {
        return (T) modules.get(clazz);
    }

    public List<ComponentMeta> getMetas() {
        return Collections.unmodifiableList(components);
    }

    public int size() {
        return components.size();
    }
}
