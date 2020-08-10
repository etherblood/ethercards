package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.implementation.statmodifiers.AddOwnSpiritCountModifier;
import java.util.function.IntUnaryOperator;

public class SpiritCountSoulshiftEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        int power = new AddOwnSpiritCountModifier().modify(data, templates, self, self, 0);
        SoulshiftEffect soulshiftEffect = new SoulshiftEffect(power);
        soulshiftEffect.apply(data, templates, random, events, self, triggerTarget);
    }
}
