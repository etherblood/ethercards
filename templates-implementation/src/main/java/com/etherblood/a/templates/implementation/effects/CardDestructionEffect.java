package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class CardDestructionEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int card : data.list(core.IN_HAND_ZONE)) {
            data.set(card, core.DISCARD, 1);
            int owner = data.get(card, core.OWNER);
            SystemsUtil.increase(data, owner, core.DRAW_CARDS_REQUEST, 1);
        }
    }
}
