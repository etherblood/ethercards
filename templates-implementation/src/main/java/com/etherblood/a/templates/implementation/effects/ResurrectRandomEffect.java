package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.updates.TriggerService;
import com.etherblood.ethercards.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class ResurrectRandomEffect implements Effect {

    public final int maxManaCost;

    public ResurrectRandomEffect(int maxManaCost) {
        this.maxManaCost = maxManaCost;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        int owner = data.get(source, core.OWNER);
        IntList candidates = new IntList();
        for (int dead : data.list(core.IN_GRAVEYARD_ZONE)) {
            if (!data.hasValue(dead, core.OWNER, owner)) {
                continue;
            }
            CardTemplate template = templates.getCard(data.get(dead, core.CARD_TEMPLATE));
            Integer manaCost = template.getHand().getCast().getManaCost();
            if (template.isMinion() && manaCost != null && manaCost <= maxManaCost) {
                candidates.add(dead);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int resurrectMinion = candidates.getRandomItem(random);

        ZoneService zoneService = new ZoneService(data, templates, random, events);
        zoneService.removeFromGraveyard(resurrectMinion);
        zoneService.addToBattle(resurrectMinion, true);
        data.set(resurrectMinion, core.SUMMONING_SICKNESS, 1);
        new TriggerService(data, templates, random, events).onSummoned(resurrectMinion);
    }
}
