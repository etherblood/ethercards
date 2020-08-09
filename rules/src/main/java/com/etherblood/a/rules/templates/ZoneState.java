package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ZoneState {
    
    private final IntMap components;
    private final Map<Integer, List<StatModifier>> componentModifiers;
    private final ActivatedAbility cast;
    private final ActivatedAbility activated;
    private final Map<Integer, List<Effect>> passive;

    public ZoneState(IntMap components, Map<Integer, List<StatModifier>> componentModifiers, ActivatedAbility cast, ActivatedAbility activated, Map<Integer, List<Effect>> passive) {
        this.components = components;
        this.componentModifiers = Objects.requireNonNull(componentModifiers);
        this.cast = cast;
        this.activated = activated;
        this.passive = Objects.requireNonNull(passive);
    }

    public IntMap getComponents() {
        return components;
    }

    public Map<Integer, List<StatModifier>> getComponentModifiers() {
        return componentModifiers;
    }

    public ActivatedAbility getCast() {
        return cast;
    }

    public ActivatedAbility getActivated() {
        return activated;
    }

    public Map<Integer, List<Effect>> getPassive() {
        return passive;
    }
}
