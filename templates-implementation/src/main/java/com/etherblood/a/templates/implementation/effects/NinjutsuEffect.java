package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
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
