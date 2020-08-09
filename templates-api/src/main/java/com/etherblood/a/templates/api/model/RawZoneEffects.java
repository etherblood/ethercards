package com.etherblood.a.templates.api.model;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.ActivatedAbility;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.StatModifier;
import java.util.List;
import java.util.Map;

public class RawZoneEffects {
    
    public IntMap components;
    public Map<Integer, List<StatModifier>> componentModifiers;
    public ActivatedAbility cast;
    public ActivatedAbility activated;
    public Map<Integer, List<Effect>> passive;
}
