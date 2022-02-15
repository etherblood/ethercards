package com.etherblood.ethercards.templates.api.model;

import com.etherblood.ethercards.rules.templates.ActivatedAbility;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.TargetSelection;
import com.etherblood.ethercards.templates.api.Untargeted;
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
