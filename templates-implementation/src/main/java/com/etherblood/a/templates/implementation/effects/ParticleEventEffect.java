package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.ParticleEvent;
import com.etherblood.a.rules.GameTemplates;
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
