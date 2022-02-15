package com.etherblood.ethercards.rules.templates;

import com.etherblood.ethercards.entities.collections.IntMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ZoneState {

    private final IntMap components;
    private final Map<Integer, StatModifier> statModifiers;
    private final ActivatedAbility cast;
    private final ActivatedAbility activated;
    private final Map<Integer, List<Effect>> passive;

    public ZoneState(IntMap components, Map<Integer, StatModifier> statModifiers, ActivatedAbility cast, ActivatedAbility activated, Map<Integer, List<Effect>> passive) {
        this.components = components;
        this.statModifiers = Objects.requireNonNull(statModifiers);
        this.cast = cast;
        this.activated = activated;
        this.passive = Objects.requireNonNull(passive);
    }

    public IntMap getComponents() {
        return components;
    }

    public Map<Integer, StatModifier> getStatModifiers() {
        return statModifiers;
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
