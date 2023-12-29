package com.etherblood.ethercards.rules.updates.systems;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.TriggerService;

import java.util.function.IntUnaryOperator;

public class CastSystem {

    private final EntityData data;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final CoreComponents core;
    private final TriggerService triggerService;

    public CastSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.templates = templates;
        this.random = random;
        this.events = events;
        this.core = data.getSchema().getModule(CoreComponents.class);
        this.triggerService = new TriggerService(data, templates, random, events);
    }

    public void run() {
        EntityList list = data.list(core.CAST_TARGET);
        if (list.isEmpty()) {
            return;
        }
        IntMap casts = new IntMap();
        for (int castSource : list) {
            int cardTemplateId = data.get(castSource, core.CARD_TEMPLATE);
            int target = data.get(castSource, core.CAST_TARGET);
            casts.set(castSource, target);
            CardTemplate template = templates.getCard(cardTemplateId);
            if (target == ~0) {
                assert template.getHand().getCast().getTarget().isValidTarget(data, templates, castSource, null);
            } else {
                assert template.getHand().getCast().getTarget().isValidTarget(data, templates, castSource, target);
            }
            int owner = data.get(castSource, core.OWNER);
            Integer manaCost = template.getHand().getCast().getManaCost();
            if (manaCost != null && manaCost != 0) {
                int mana = data.get(owner, core.MANA);
                mana -= manaCost;
                if (mana < 0) {
                    throw new IllegalStateException();
                }
                data.set(owner, core.MANA, mana);
            }
            for (Effect effect : template.getHand().getCast().getEffects()) {
                effect.apply(data, templates, random, events, castSource, target);
            }
        }
        data.clear(core.CAST_TARGET);
        for (int castSource : casts) {
            triggerService.onCast(castSource, casts.get(castSource));
        }
    }
}
