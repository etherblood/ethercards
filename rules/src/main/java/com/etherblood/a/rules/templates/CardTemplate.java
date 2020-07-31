package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CardTemplate {

    private final int id;
    private final boolean isMinion;
    private final Integer manaCost;
    private final TargetSelection castTarget;
    private final List<Effect> castEffects;
    protected final IntMap components;
    protected final Set<Tribe> tribes;
    protected final Map<Integer, List<Effect>> inBattle;
    protected final Map<Integer, List<Effect>> inHand;
    protected final Map<Integer, List<Effect>> inGraveyard;
    protected final Map<Integer, List<StatModifier>> componentModifiers;

    protected CardTemplate(int id, boolean isMinion, Integer manaCost, TargetSelection castTarget, List<Effect> castEffects, IntMap components, Set<Tribe> tribes, Map<Integer, List<Effect>> inBattle, Map<Integer, List<Effect>> inHand, Map<Integer, List<Effect>> inGraveyard, Map<Integer, List<StatModifier>> componentModifiers) {
        this.id = id;
        this.isMinion = isMinion;
        this.manaCost = manaCost;
        this.castTarget = Objects.requireNonNull(castTarget);
        this.castEffects = Collections.unmodifiableList(new ArrayList<>(castEffects));
        this.tribes = Collections.unmodifiableSet(EnumSet.copyOf(tribes));
        this.inBattle = deepCopy(inBattle);
        this.inHand = deepCopy(inHand);
        this.inGraveyard = deepCopy(inGraveyard);
        this.components = new IntMap();
        for (int key : components) {
            this.components.set(key, components.get(key));
        }
        this.componentModifiers = Collections.unmodifiableMap(componentModifiers.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> Collections.unmodifiableList(new ArrayList<>(x.getValue())))));
    }
    
    private <K, V> Map<K, List<V>> deepCopy(Map<K, List<V>> map) {
        Map<K, List<V>> result = new HashMap<>();
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public Integer getManaCost() {
        return manaCost;
    }

    public TargetSelection getCastTarget() {
        return castTarget;
    }

    public List<Effect> getCastEffects() {
        return castEffects;
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
    
    public Map<Integer, List<Effect>> getBattleTriggers() {
        return inBattle;
    }
    
    public Map<Integer, List<Effect>> getHandTriggers() {
        return inHand;
    }
    
    public Map<Integer, List<Effect>> getGraveyardTriggers() {
        return inGraveyard;
    }
    
    public Map<Integer, List<Effect>> getLibraryTriggers() {
        return Collections.emptyMap();
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
