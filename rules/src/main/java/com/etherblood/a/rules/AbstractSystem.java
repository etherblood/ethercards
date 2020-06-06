package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import java.util.function.IntUnaryOperator;


public abstract class AbstractSystem {

    public abstract void run(GameSettings settings, EntityData data, IntUnaryOperator random);
}
