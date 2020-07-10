package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CardTemplate {

    private final int id;
    private final boolean isMinion;
    private final Integer manaCost;
    private final CardCast[] casts;
    protected final IntMap components;
    protected final Set<Tribe> tribes;
    protected final List<Effect> onCastEffects;
    protected final List<Effect> onSummonEffects;
    protected final List<Effect> onSelfDeathEffects;
    protected final List<Effect> onSelfSurviveEffects;
    protected final List<Effect> onSelfUpkeepEffects;
    protected final List<Effect> afterSelfBattleEffects;
    protected final Map<Integer, List<StatModifier>> componentModifiers;

    protected CardTemplate(int id, boolean isMinion, Integer manaCost, CardCast[] casts, IntMap components, Set<Tribe> tribes, List<Effect> onCastEffects, List<Effect> onSummonEffects, List<Effect> onDeathEffects, List<Effect> onSurviveEffects, List<Effect> onUpkeepEffects, List<Effect> afterBattleEffects, Map<Integer, List<StatModifier>> componentModifiers) {
        this.id = id;
        this.isMinion = isMinion;
        this.manaCost = manaCost;
        this.casts = casts;
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
        this.componentModifiers = Collections.unmodifiableMap(componentModifiers.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> Collections.unmodifiableList(new ArrayList<>(x.getValue())))));
    }

    public int getId() {
        return id;
    }

    public Integer getManaCost() {
        return manaCost;
    }

    public CardCast getAttackPhaseCast() {
        return Arrays.stream(casts).filter(c -> c.isAttackCast()).findFirst().orElse(null);
    }

    public CardCast getBlockPhaseCast() {
        return Arrays.stream(casts).filter(c -> c.isBlockCast()).findFirst().orElse(null);
    }

    public CardCast[] getCasts() {
        return casts;
    }

    public String getTemplateName() {
        return "#" + id;
    }

    public Iterable<Integer> components() {
        return components;
    }

    public int get(int component) {
        return components.get(component);
    }

    public boolean has(int component) {
        return components.hasKey(component);
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

    public List<StatModifier> getComponentModifiers(int component) {
        return componentModifiers.getOrDefault(component, Collections.emptyList());
    }

    public Set<Tribe> getTribes() {
        return tribes;
    }

    public boolean isMinion() {
        return isMinion;
    }
}
