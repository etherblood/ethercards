package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import java.util.function.IntUnaryOperator;


public abstract class AbstractSystem {

    public abstract void run(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener eventListener);
}
