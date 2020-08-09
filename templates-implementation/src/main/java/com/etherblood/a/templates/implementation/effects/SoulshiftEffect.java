package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class SoulshiftEffect implements Effect {

    public final int power;

    public SoulshiftEffect(int power) {
        this.power = power;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNER);
        int best = -1;
        IntList candidates = new IntList();
        for (int candidate : data.list(core.IN_GRAVEYARD_ZONE)) {
            if (!data.hasValue(candidate, core.OWNER, owner)) {
                continue;
            }
            int templateId = data.get(candidate, core.CARD_TEMPLATE);
            CardTemplate template = templates.getCard(templateId);
            if (template.getTribes().contains(Tribe.SPIRIT)) {
                Integer manaCost = template.getHand().getCast().getManaCost();
                if (manaCost != null) {
                    if (manaCost > power) {
                        continue;
                    }
                    if (manaCost > best) {
                        candidates.clear();
                        best = manaCost;
                    }
                    if (manaCost == best) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int selected = candidates.getRandomItem(random);
        ZoneService zoneService = new ZoneService(data, templates, random, events);
        zoneService.removeFromGraveyard(selected);
        zoneService.addToHand(selected);
    }
}
