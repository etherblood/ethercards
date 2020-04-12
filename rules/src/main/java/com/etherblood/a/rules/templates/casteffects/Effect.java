package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.Game;

public abstract class Effect {

    public abstract void apply(Game game, EntityData data, int source, int target);
}
