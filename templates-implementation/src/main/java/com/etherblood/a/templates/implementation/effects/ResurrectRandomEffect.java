package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class ResurrectRandomEffect implements Effect {

    public final int maxManaCost;

    public ResurrectRandomEffect(int maxManaCost) {
        this.maxManaCost = maxManaCost;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        int owner = data.get(source, core.OWNED_BY);
        IntList candidates = new IntList();
        for (int dead : data.list(core.IN_GRAVEYARD_ZONE)) {
            if (!data.hasValue(dead, core.OWNED_BY, owner)) {
                continue;
            }
            CardTemplate template = templates.getCard(data.get(dead, core.CARD_TEMPLATE));
            if (template.isMinion() && template.getManaCost() != null && template.getManaCost() <= maxManaCost) {
                candidates.add(dead);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int resurrectMinion = candidates.getRandomItem(random);

        ZoneService zoneService = new ZoneService(data, templates);
        zoneService.removeFromGraveyard(resurrectMinion);
        zoneService.addToBattle(resurrectMinion);
        data.set(resurrectMinion, core.SUMMONING_SICKNESS, 1);
        for (int other : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (resurrectMinion == other) {
                continue;
            }
            int otherTemplateId = data.get(other, core.CARD_TEMPLATE);
            CardTemplate otherTemplate = templates.getCard(otherTemplateId);
            for (Effect effect : otherTemplate.getOnSummonEffects()) {
                effect.apply(data, templates, random, events, other, resurrectMinion);
            }
        }
    }
}
