package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.rules.templates.casteffects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class SummonEffect extends CastEffect {

    @MinionId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }

    @Override
    public void cast(Game game, EntityData data, int source, int target) {
        SystemsUtil.summon(game, minionId, data.get(source, Components.OWNED_BY));
    }
}
