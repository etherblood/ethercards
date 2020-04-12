package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.rules.templates.casteffects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class SummonEffect extends Effect {

    @MinionId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }

    @Override
    public void apply(Game game, EntityData data, int source, int target) {
        SystemsUtil.summon(game, minionId, data.get(source, data.getComponents().getModule(CoreComponents.class).OWNED_BY));
    }
}
