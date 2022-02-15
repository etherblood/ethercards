package com.etherblood.ethercards.templates.api.model;

import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.StatModifier;
import com.etherblood.ethercards.rules.templates.ZoneState;
import java.util.List;
import java.util.Map;

public class RawZoneState {

    public IntMap components;
    public Map<Integer, StatModifier> statModifiers;
    public RawActivatedAbility cast;
    public RawActivatedAbility activated;
    public Map<Integer, List<Effect>> passive;

    public RawZoneState() {
    }

    public ZoneState toZoneState() {
        return new ZoneState(
                components,
                statModifiers,
                RawActivatedAbility.toActivatedAbility(cast),
                RawActivatedAbility.toActivatedAbility(activated),
                passive);
    }
}
