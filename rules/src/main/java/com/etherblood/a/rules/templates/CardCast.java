package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.templates.effects.Effect;
import com.etherblood.a.rules.templates.effects.targeting.TargetFilters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardCast {

    private final boolean attackCast, blockCast;
    private final int manaCost;
    private final TargetFilters[] targets;
    private final List<Effect> effects;

    CardCast(int manaCost, TargetFilters[] targets, List<Effect> effects, boolean attackCast, boolean blockCast) {
        this.manaCost = manaCost;
        this.targets = targets;
        this.effects = Collections.unmodifiableList(new ArrayList<>(effects));
        this.attackCast = attackCast;
        this.blockCast = blockCast;
    }

    public int getManaCost() {
        return manaCost;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public boolean isTargeted() {
        return targets != null && targets.length != 0;
    }

    public TargetFilters[] getTargets() {
        return targets;
    }

    public boolean isAttackCast() {
        return attackCast;
    }

    public boolean isBlockCast() {
        return blockCast;
    }
}
