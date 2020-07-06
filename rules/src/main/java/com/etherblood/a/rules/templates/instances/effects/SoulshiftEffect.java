package com.etherblood.a.rules.templates.instances.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Tribe;
import java.util.function.IntUnaryOperator;

public class SoulshiftEffect implements Effect {

    public final int power;

    public SoulshiftEffect(int power) {
        this.power = power;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNED_BY);
        int best = -1;
        IntList candidates = new IntList();
        for (int candidate : data.list(core.IN_GRAVEYARD_ZONE)) {
            if (!data.hasValue(candidate, core.OWNED_BY, owner)) {
                continue;
            }
            int templateId = data.get(candidate, core.CARD_TEMPLATE);
            CardTemplate template = templates.getCard(templateId);
            if (template.getTribes().contains(Tribe.SPIRIT)) {
                if (template.getManaCost() != null) {
                    if (template.getManaCost() > power) {
                        continue;
                    }
                    if (template.getManaCost() > best) {
                        candidates.clear();
                        best = template.getManaCost();
                    }
                    if (template.getManaCost() == best) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int selected = candidates.getRandomItem(random);
        data.remove(selected, core.IN_GRAVEYARD_ZONE);
        data.set(selected, core.IN_HAND_ZONE, 1);
    }
}
