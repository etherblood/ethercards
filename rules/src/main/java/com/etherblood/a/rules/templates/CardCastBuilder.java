package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.targeting.TargetFilters;
import java.util.ArrayList;
import java.util.List;

public class CardCastBuilder {

    private boolean attackCast = true, blockCast = true;
    private TargetFilters[] targets;
    private final List<Effect> effects = new ArrayList<>();

    public void setTargets(TargetFilters[] targets) {
        this.targets = targets;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public void setAttackCast(boolean attackCast) {
        this.attackCast = attackCast;
    }

    public void setBlockCast(boolean blockCast) {
        this.blockCast = blockCast;
    }

    public CardCast build() {
        if (effects.isEmpty()) {
            return null;
        }
        return new CardCast(targets, effects, attackCast, blockCast);
    }

}
