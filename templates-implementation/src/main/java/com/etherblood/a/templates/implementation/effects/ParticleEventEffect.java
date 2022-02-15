package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.game.events.api.events.ParticleEvent;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import java.util.function.IntUnaryOperator;

public class ParticleEventEffect implements Effect {

    public final String alias;

    public ParticleEventEffect(String alias) {
        this.alias = alias;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        events.fire(new ParticleEvent(alias, source, target));
    }

}
