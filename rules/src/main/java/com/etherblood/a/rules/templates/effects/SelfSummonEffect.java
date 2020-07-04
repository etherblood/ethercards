package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.function.IntUnaryOperator;

public class SelfSummonEffect extends Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.remove(source, core.IN_HAND_ZONE);
        data.set(source, core.IN_BATTLE_ZONE, 1);
        data.set(source, core.SUMMONING_SICKNESS, 1);
        for (int other : data.list(core.IN_BATTLE_ZONE)) {
            if (source == other) {
                continue;
            }
            int otherTemplateId = data.get(other, core.CARD_TEMPLATE);
            CardTemplate otherTemplate = templates.getCard(otherTemplateId);
            for (Effect effect : otherTemplate.getOnSummonEffects()) {
                effect.apply(data, templates, random, events, other, source);
            }
        }
    }
}
