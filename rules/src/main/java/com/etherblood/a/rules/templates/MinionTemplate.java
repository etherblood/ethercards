package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.casteffects.Effect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MinionTemplate implements Iterable<Integer> {

    protected final int id;
    protected final IntMap components;
    protected final List<Effect> onDeathEffects;
    protected final List<Effect> onSurviveEffects;

    public MinionTemplate(int id, IntMap components, List<Effect> onDeathEffects, List<Effect> onSurviveEffects) {
        this.id = id;
        this.onDeathEffects = Collections.unmodifiableList(new ArrayList<>(onDeathEffects));
        this.onSurviveEffects = Collections.unmodifiableList(new ArrayList<>(onSurviveEffects));
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

    public List<Effect> getOnDeathEffects() {
        return onDeathEffects;
    }

    public List<Effect> getOnSurviveEffects() {
        return onSurviveEffects;
    }

    @Override
    public Iterator<Integer> iterator() {
        return components.iterator();
    }

    public String getTemplateName() {
        return "#" + id;
    }
}
