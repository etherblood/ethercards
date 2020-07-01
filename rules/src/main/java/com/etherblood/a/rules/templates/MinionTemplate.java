package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MinionTemplate implements Iterable<Integer> {

    protected final int id;
    protected final IntMap components;
    protected final Set<Tribe> tribes;
    protected final List<Effect> onCastEffects;
    protected final List<Effect> onSummonEffects;
    protected final List<Effect> onSelfDeathEffects;
    protected final List<Effect> onSelfSurviveEffects;
    protected final List<Effect> onSelfUpkeepEffects;
    protected final List<Effect> afterSelfBattleEffects;

    public MinionTemplate(int id, IntMap components, Set<Tribe> tribes, List<Effect> onCastEffects, List<Effect> onSummonEffects, List<Effect> onDeathEffects, List<Effect> onSurviveEffects, List<Effect> onUpkeepEffects, List<Effect> afterBattleEffects) {
        this.id = id;
        this.tribes = Collections.unmodifiableSet(EnumSet.copyOf(tribes));
        this.onCastEffects = Collections.unmodifiableList(new ArrayList<>(onCastEffects));
        this.onSummonEffects = Collections.unmodifiableList(new ArrayList<>(onSummonEffects));
        this.onSelfDeathEffects = Collections.unmodifiableList(new ArrayList<>(onDeathEffects));
        this.onSelfSurviveEffects = Collections.unmodifiableList(new ArrayList<>(onSurviveEffects));
        this.onSelfUpkeepEffects = Collections.unmodifiableList(new ArrayList<>(onUpkeepEffects));
        this.afterSelfBattleEffects = Collections.unmodifiableList(new ArrayList<>(afterBattleEffects));
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

    public List<Effect> getOnSummonEffects() {
        return onSummonEffects;
    }

    public List<Effect> getOnCastEffects() {
        return onCastEffects;
    }

    public List<Effect> getOnSelfDeathEffects() {
        return onSelfDeathEffects;
    }

    public List<Effect> getOnSelfSurviveEffects() {
        return onSelfSurviveEffects;
    }

    public List<Effect> getOnSelfUpkeepEffects() {
        return onSelfUpkeepEffects;
    }

    public List<Effect> getAfterSelfBattleEffects() {
        return afterSelfBattleEffects;
    }

    public Set<Tribe> getTribes() {
        return tribes;
    }

    @Override
    public Iterator<Integer> iterator() {
        return components.iterator();
    }

    public String getTemplateName() {
        return "#" + id;
    }
}
