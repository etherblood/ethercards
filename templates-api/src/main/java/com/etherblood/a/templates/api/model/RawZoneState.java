package com.etherblood.a.templates.api.model;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.ZoneState;
import java.util.List;
import java.util.Map;

public class RawZoneState {

    public IntMap components;
    public Map<Integer, List<StatModifier>> componentModifiers;
    public RawActivatedAbility cast;
    public RawActivatedAbility activated;
    public Map<Integer, List<Effect>> passive;

    public RawZoneState() {
    }

    public ZoneState toZoneState() {
        return new ZoneState(
                components,
                componentModifiers,
                RawActivatedAbility.toActivatedAbility(cast),
                RawActivatedAbility.toActivatedAbility(activated),
                passive);
    }
}
