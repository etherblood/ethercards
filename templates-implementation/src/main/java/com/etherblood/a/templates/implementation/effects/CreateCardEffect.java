package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.templates.api.filedtypes.CardId;
import com.etherblood.a.templates.api.filedtypes.ComponentsMap;
import java.util.function.IntUnaryOperator;

public class CreateCardEffect implements Effect {

    @CardId
    public final int cardId;
    @ComponentsMap
    public final IntMap components;

    public CreateCardEffect(int cardId, IntMap components) {
        this.cardId = cardId;
        this.components = components;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int entity = SystemsUtil.createCard(data, cardId, data.get(source, core.OWNER));
        for (int component : components) {
            data.set(entity, component, components.get(component));
        }
    }
}
