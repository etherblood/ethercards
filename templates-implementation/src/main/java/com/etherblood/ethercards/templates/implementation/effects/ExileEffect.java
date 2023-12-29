package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.ComponentMeta;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;

import java.util.function.IntUnaryOperator;

public class ExileEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        for (ComponentMeta meta : data.getSchema().getMetas()) {
            data.remove(target, meta.id);
        }
    }
}
