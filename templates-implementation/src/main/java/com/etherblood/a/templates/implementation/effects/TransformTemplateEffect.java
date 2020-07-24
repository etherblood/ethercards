package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.deserializers.filedtypes.CardId;
import java.util.function.IntUnaryOperator;

public class TransformTemplateEffect implements Effect {

    @CardId
    public final int template;

    public TransformTemplateEffect(int template) {
        this.template = template;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.set(target, core.ORIGINAL_CARD_TEMPLATE, data.get(target, core.CARD_TEMPLATE));
        data.set(target, core.CARD_TEMPLATE, template);
    }
}
