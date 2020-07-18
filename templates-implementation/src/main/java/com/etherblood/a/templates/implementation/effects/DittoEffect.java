package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

public class DittoEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.set(source, core.ORIGINAL_CARD_TEMPLATE, data.get(source, core.CARD_TEMPLATE));
        IntList whitelist = new IntList(core.OWNER, core.TEAM, core.IN_BATTLE_ZONE, core.IN_GRAVEYARD_ZONE, core.IN_HAND_ZONE, core.IN_LIBRARY_ZONE, core.TIRED, core.ORIGINAL_CARD_TEMPLATE);
        for (ComponentMeta meta : data.getComponents().getMetas()) {
            if(whitelist.contains(meta.id)) {
                continue;
            }
            OptionalInt value = data.getOptional(target, meta.id);
            if(value.isPresent()) {
                data.set(source, meta.id, value.getAsInt());
            } else {
                data.remove(source, meta.id);
            }
        }
    }
}
