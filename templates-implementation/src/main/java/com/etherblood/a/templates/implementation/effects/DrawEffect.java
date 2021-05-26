package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class DrawEffect implements Effect {

    public final int amount;

    public DrawEffect(int amount) {
        this.amount = amount;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        SystemsUtil.drawCards(data, templates, random, events, target, amount);
    }
}
