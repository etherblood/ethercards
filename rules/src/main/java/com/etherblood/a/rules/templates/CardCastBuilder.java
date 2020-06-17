package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.templates.effects.Effect;
import com.etherblood.a.rules.templates.effects.targeting.TargetFilters;
import java.util.ArrayList;
import java.util.List;

public class CardCastBuilder {

    private int manaCost;
    private TargetFilters[] targets;
    private final List<Effect> effects = new ArrayList<>();

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public void setTargets(TargetFilters[] targets) {
        this.targets = targets;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public CardCast build() {
        if (effects.isEmpty()) {
            return null;
        }
        return new CardCast(manaCost, targets, effects);
    }

}
