package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.Tribe;
import java.util.function.IntUnaryOperator;

public class TribeActivatedEffects implements Effect {

    public final Tribe tribe;
    public final Effect[] effects;

    public TribeActivatedEffects(Tribe tribe, Effect[] effects) {
        this.tribe = tribe;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNER);
        boolean tribeExists = false;
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.hasValue(minion, core.OWNER, owner)) {
                int templateId = data.get(minion, core.CARD_TEMPLATE);
                CardTemplate card = templates.getCard(templateId);
                if (card.getTribes().contains(tribe)) {
                    tribeExists = true;
                    break;
                }
            }
        }
        if (tribeExists) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
