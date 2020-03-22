package com.etherblood.a.rules.templates;

import com.etherblood.a.rules.templates.casteffects.CastEffect;
import java.util.ArrayList;
import java.util.List;

public class CardCastBuilder {

    private int manaCost;
    private boolean targeted;
    private final List<CastEffect> effects = new ArrayList();

    CardCastBuilder() {
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
    }

    public void addEffect(CastEffect effect) {
        effects.add(effect);
    }

    public CardCast build() {
        if (effects.isEmpty()) {
            return null;
        }
        return new CardCast(manaCost, targeted, effects);
    }

}
