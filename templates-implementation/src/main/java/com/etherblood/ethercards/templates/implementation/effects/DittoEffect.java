package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.ComponentMeta;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.TriggerService;

import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

public class DittoEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        if (!data.has(source, core.ORIGINAL_CARD_TEMPLATE)) {
            data.set(source, core.ORIGINAL_CARD_TEMPLATE, data.get(source, core.CARD_TEMPLATE));
        }
        TriggerService triggerService = new TriggerService(data, templates, random, events);
        triggerService.cleanupEffects(source);
        IntList blacklist = new IntList(
                core.OWNER,
                core.TEAM,
                core.IN_BATTLE_ZONE,
                core.IN_GRAVEYARD_ZONE,
                core.IN_HAND_ZONE,
                core.IN_LIBRARY_ZONE,
                core.TIRED,
                core.ORIGINAL_CARD_TEMPLATE,
                core.ATTACK_TARGET,
                core.BLOCK_TARGET,
                core.BLOCKED,
                core.SUMMONING_SICKNESS);
        for (ComponentMeta meta : data.getSchema().getMetas()) {
            if (blacklist.contains(meta.id)) {
                continue;
            }
            OptionalInt value = data.getOptional(target, meta.id);
            if (value.isPresent()) {
                data.set(source, meta.id, value.getAsInt());
            } else {
                data.remove(source, meta.id);
            }
        }
        triggerService.initEffects(source);
        triggerService.onEnterBattle(source);// FIXME: trigger misuse
    }
}
