package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.Game;

public abstract class CastEffect {

    public abstract void cast(Game game, EntityData data, int source, int target);
}
