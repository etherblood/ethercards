package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class SelfSummonEffect implements Effect {

    private final boolean skipComponents;

    public SelfSummonEffect(boolean skipComponents) {
        this.skipComponents = skipComponents;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.remove(source, core.IN_HAND_ZONE);
        new ZoneService(data, templates).addToBattle(source, !skipComponents);
        data.set(source, core.SUMMONING_SICKNESS, 1);
        for (int other : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            int otherTemplateId = data.get(other, core.CARD_TEMPLATE);
            CardTemplate otherTemplate = templates.getCard(otherTemplateId);
            for (Effect effect : otherTemplate.getOnSummonEffects()) {
                effect.apply(data, templates, random, events, other, source);
            }
        }
    }
}
