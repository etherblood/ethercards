package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;

import java.util.Random;

public abstract class AbstractSystem {

    public abstract void run(EntityData data, Random random);
}
