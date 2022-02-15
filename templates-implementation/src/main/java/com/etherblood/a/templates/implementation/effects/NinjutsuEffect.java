package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public class NinjutsuEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        assert data.list(core.NINJUTSU_TARGET).stream().noneMatch(x -> x == target);
        data.set(source, core.NINJUTSU_ORDER, data.createEntity());
        data.set(source, core.NINJUTSU_TARGET, target);
    }
}
