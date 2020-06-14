package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class SingleTargetDamageEffect extends Effect {

    public final int damage;

    public SingleTargetDamageEffect(int damage) {
        this.damage = damage;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, int source, int target) {
        SystemsUtil.damage(data, target, damage);
    }
}
