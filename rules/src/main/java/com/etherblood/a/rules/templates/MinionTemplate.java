package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public class MinionTemplate implements Iterable<Integer> {

    protected final int id;
    protected final IntMap components;
    protected final List<Effect> onDeathEffects;
    protected final List<Effect> onSurviveEffects;
    protected final List<Effect> onUpkeepEffects;
    protected final List<Effect> afterBattleEffects;

    public MinionTemplate(int id, IntMap components, List<Effect> onDeathEffects, List<Effect> onSurviveEffects, List<Effect> onUpkeepEffects, List<Effect> afterBattleEffects) {
        this.id = id;
        this.onDeathEffects = Collections.unmodifiableList(new ArrayList<>(onDeathEffects));
        this.onSurviveEffects = Collections.unmodifiableList(new ArrayList<>(onSurviveEffects));
        this.onUpkeepEffects = Collections.unmodifiableList(new ArrayList<>(onUpkeepEffects));
        this.afterBattleEffects = Collections.unmodifiableList(new ArrayList<>(afterBattleEffects));
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

    public boolean has(int component, int value) {
        return components.getOrElse(component, ~value) == value;
    }

    public List<Effect> getOnDeathEffects() {
        return onDeathEffects;
    }

    public List<Effect> getOnSurviveEffects() {
        return onSurviveEffects;
    }

    public List<Effect> getOnUpkeepEffects() {
        return onUpkeepEffects;
    }

    public List<Effect> getAfterBattleEffects() {
        return afterBattleEffects;
    }

    @Override
    public Iterator<Integer> iterator() {
        return components.iterator();
    }

    public String getTemplateName() {
        return "#" + id;
    }
}
