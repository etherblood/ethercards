package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.List;

public class CardCastBuilder {

    private int manaCost;
    private boolean targeted;
    private final List<Effect> effects = new ArrayList();

    CardCastBuilder() {
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public CardCast build() {
        if (effects.isEmpty()) {
            return null;
        }
        return new CardCast(manaCost, targeted, effects);
    }

}
