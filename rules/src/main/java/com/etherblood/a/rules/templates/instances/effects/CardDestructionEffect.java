package com.etherblood.a.rules.templates.instances.effects;

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
            int owner = data.get(card, core.OWNED_BY);
            SystemsUtil.increase(data, owner, core.DISCARD_CARDS_REQUEST, 1);
            SystemsUtil.increase(data, owner, core.DRAW_CARDS_REQUEST, 1);
        }
    }
}
