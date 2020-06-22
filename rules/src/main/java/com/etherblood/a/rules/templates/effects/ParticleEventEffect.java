package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.ParticleEvent;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class ParticleEventEffect extends Effect {

    public final String alias;

    public ParticleEventEffect(String alias) {
        this.alias = alias;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener events, int source, int target) {
        events.fire(new ParticleEvent(alias, source, target));
    }

}
