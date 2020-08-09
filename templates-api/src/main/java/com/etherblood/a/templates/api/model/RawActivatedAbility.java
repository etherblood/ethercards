package com.etherblood.a.templates.api.model;

import com.etherblood.a.rules.templates.ActivatedAbility;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.TargetSelection;
import com.etherblood.a.templates.api.Untargeted;
import java.util.List;

public class RawActivatedAbility {

    public TargetSelection target;
    public List<Effect> effects;
    public Integer manaCost;
    public boolean selfTap;

    public ActivatedAbility toActivatedAbility() {
        return new ActivatedAbility(target != null ? target : new Untargeted(), effects, manaCost, selfTap);
    }

    public static ActivatedAbility toActivatedAbility(RawActivatedAbility raw) {
        return raw == null ? null : raw.toActivatedAbility();
    }
}
