package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.CardId;

import java.util.function.IntUnaryOperator;

public class CreateCardEffect implements Effect {

    @CardId
    public final int cardId;
    public final IntMap components;

    public CreateCardEffect(int cardId, IntMap components) {
        this.cardId = cardId;
        this.components = components;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        int entity = SystemsUtil.createCard(data, cardId, data.get(source, core.OWNER));
        for (int component : components) {
            data.set(entity, component, components.get(component));
        }
    }
}
