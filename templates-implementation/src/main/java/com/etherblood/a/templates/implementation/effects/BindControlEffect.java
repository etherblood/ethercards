package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public class BindControlEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (!data.has(target, core.ORIGINAL_OWNER)) {
            data.set(target, core.ORIGINAL_OWNER, data.get(target, core.OWNER));
        }
        if (!data.has(target, core.ORIGINAL_TEAM)) {
            data.set(target, core.ORIGINAL_TEAM, data.get(target, core.TEAM));
        }
        int owner = data.get(source, core.OWNER);
        data.set(target, core.OWNER, owner);
        int team = data.get(source, core.TEAM);
        data.set(target, core.TEAM, team);
        data.set(target, core.BOUND_TO, source);
        data.set(target, core.SUMMONING_SICKNESS, 1);
    }
}
