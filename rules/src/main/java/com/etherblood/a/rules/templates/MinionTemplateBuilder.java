package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.Components;

public class MinionTemplateBuilder {

    private static final IntMap DEFAULT_COMPONENTS;

    protected final int id;
    protected final IntMap components = new IntMap();

    static {
        DEFAULT_COMPONENTS = new IntMap();
        DEFAULT_COMPONENTS.set(Components.ATTACK, 0);
        DEFAULT_COMPONENTS.set(Components.HEALTH, 0);
        DEFAULT_COMPONENTS.set(Components.TIRED, 1);
    }

    protected MinionTemplateBuilder(int id) {
        this.id = id;
        for (int key : DEFAULT_COMPONENTS) {
            components.set(key, DEFAULT_COMPONENTS.get(key));
        }
    }

    public void set(int component, int value) {
        components.set(component, value);
    }

    public void remove(int component) {
        components.remove(component);
    }

    public MinionTemplate build() {
        components.set(Components.MINION_TEMPLATE, id);
        MinionTemplate minionTemplate = new MinionTemplate(id, components);
        return minionTemplate;
    }

}
