package com.etherblood.a.rules.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivatedAbility {

    private final TargetSelection target;
    private final List<Effect> effects;
    private final Integer manaCost;
    private final boolean selfTap;

    public ActivatedAbility(TargetSelection target, List<Effect> effects, Integer manaCost, boolean selfTap) {
        this.target = target;
        this.effects = Collections.unmodifiableList(new ArrayList<>(effects));
        this.manaCost = manaCost;
        this.selfTap = selfTap;
    }

    public TargetSelection getTarget() {
        return target;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public Integer getManaCost() {
        return manaCost;
    }

    public boolean isSelfTap() {
        return selfTap;
    }
}
