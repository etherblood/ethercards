package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.deserializers.filedtypes.CardId;
import com.etherblood.ethercards.rules.updates.SystemsUtil;

import java.util.function.IntUnaryOperator;

public class SummonEffect implements Effect {

    @CardId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNER);
        SystemsUtil.summonMinion(data, templates, random, events, minionId, owner);
    }
}
