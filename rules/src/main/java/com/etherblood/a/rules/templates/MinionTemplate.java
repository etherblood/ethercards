package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import java.util.Iterator;

public class MinionTemplate implements Iterable<Integer> {

    protected final int id;
    protected final IntMap components;

    public MinionTemplate(int id, IntMap components) {
        this.id = id;
        this.components = new IntMap();
        for (int key : components) {
            this.components.set(key, components.get(key));
        }
    }

    public int getId() {
        return id;
    }
    
    public int get(int component) {
        return components.get(component);
    }

    @Override
    public Iterator<Integer> iterator() {
        return components.iterator();
    }

    public String getTemplateName() {
        return "#" + id;
    }
}
