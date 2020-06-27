package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.rules.templates.effects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class SummonEffect extends Effect {

    @MinionId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNED_BY);
        int summon = SystemsUtil.createMinion(data, templates, random, minionId, owner);
        data.set(summon, core.SUMMONING_SICKNESS, 1);
    }
}
