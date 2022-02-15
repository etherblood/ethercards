package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.templates.implementation.statmodifiers.AddOwnSpiritCountModifier;
import java.util.function.IntUnaryOperator;

public class SpiritCountSoulshiftEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        int power = new AddOwnSpiritCountModifier().modify(data, templates, self, self, 0);
        SoulshiftEffect soulshiftEffect = new SoulshiftEffect(power);
        soulshiftEffect.apply(data, templates, random, events, self, triggerTarget);
    }
}
