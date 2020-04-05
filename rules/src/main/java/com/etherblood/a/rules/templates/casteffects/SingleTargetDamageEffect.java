package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class SingleTargetDamageEffect extends CastEffect {

    public final int damage;

    public SingleTargetDamageEffect(int damage) {
        this.damage = damage;
    }

    @Override
    public void cast(Game game, EntityData data, int source, int target) {
        SystemsUtil.damage(data, target, damage);
    }
}
