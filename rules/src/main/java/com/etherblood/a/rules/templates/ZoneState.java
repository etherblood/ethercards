package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import java.util.List;
import java.util.Map;

public class ZoneState {
    
    public final IntMap components;
    public final Map<Integer, List<StatModifier>> componentModifiers;
    public final ActivatedAbility cast;
    public final ActivatedAbility activated;
    public final Map<Integer, List<Effect>> passive;

    public ZoneState(IntMap components, Map<Integer, List<StatModifier>> componentModifiers, ActivatedAbility cast, ActivatedAbility activated, Map<Integer, List<Effect>> passive) {
        this.components = components;
        this.componentModifiers = componentModifiers;
        this.cast = cast;
        this.activated = activated;
        this.passive = passive;
    }
}
