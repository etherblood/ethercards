package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardCast {

    private final int manaCost;
    private final boolean targeted;
    private final List<Effect> effects;

    CardCast(int manaCost, boolean targeted, List<Effect> effects) {
        this.manaCost = manaCost;
        this.targeted = targeted;
        this.effects = Collections.unmodifiableList(new ArrayList<>(effects));
    }

    public int getManaCost() {
        return manaCost;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public boolean isTargeted() {
        return targeted;
    }
}
