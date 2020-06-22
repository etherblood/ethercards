package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public abstract class Effect {

    public abstract void apply(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener events, int source, int target);
}
