package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.templates.effects.filedtypes.CardId;
import com.etherblood.a.rules.templates.effects.filedtypes.ComponentsMap;
import java.util.function.IntUnaryOperator;

public class CreateCardEffect extends Effect {

    @CardId
    public final int cardId;
    @ComponentsMap
    public final IntMap components;

    public CreateCardEffect(int cardId, IntMap components) {
        this.cardId = cardId;
        this.components = components;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int entity = data.createEntity();
        data.set(entity, core.CARD_TEMPLATE, cardId);
        data.set(entity, core.OWNED_BY, data.get(source, core.OWNED_BY));
        for (int component : components) {
            data.set(entity, component, components.get(component));
        }
    }
}
