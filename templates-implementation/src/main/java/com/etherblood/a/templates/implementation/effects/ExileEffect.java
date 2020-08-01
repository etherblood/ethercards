package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public class ExileEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        for (ComponentMeta meta : data.getComponents().getMetas()) {
            data.remove(target, meta.id);
        }
    }
}
