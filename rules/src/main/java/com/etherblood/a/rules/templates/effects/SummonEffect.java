package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.rules.templates.effects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class SummonEffect extends Effect {

    @MinionId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }

    @Override
    public void apply(GameSettings settings, EntityData data, IntUnaryOperator random, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(source, core.OWNED_BY);
        SystemsUtil.summon(settings, data, minionId, owner);
    }
}
