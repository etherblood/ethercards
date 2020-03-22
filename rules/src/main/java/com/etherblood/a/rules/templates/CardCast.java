package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.templates.casteffects.CastEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardCast {

    private final int manaCost;
    private final boolean targeted;
    private final List<CastEffect> effects;

    CardCast(int manaCost, boolean targeted, List<CastEffect> effects) {
        this.manaCost = manaCost;
        this.targeted = targeted;
        this.effects = Collections.unmodifiableList(new ArrayList<>(effects));
    }

    public int getManaCost() {
        return manaCost;
    }

    public List<CastEffect> getEffects() {
        return effects;
    }

    public boolean isTargeted() {
        return targeted;
    }
}
