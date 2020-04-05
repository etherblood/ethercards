package com.etherblood.a.rules.templates.casteffects;

import com.etherblood.a.rules.templates.casteffects.filedtypes.MinionId;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.MinionTemplate;

public class SummonEffect extends CastEffect {

    @MinionId
    public final int minionId;

    public SummonEffect(int minionId) {
        this.minionId = minionId;
    }

    @Override
    public void cast(Game game, EntityData data, int source, int target) {
        int minionTemplate = minionId;
        MinionTemplate minion = game.getMinions().apply(minionTemplate);
        int entity = data.createEntity();
        for (int component : minion) {
            data.set(entity, component, minion.get(component));
        }
        data.set(entity, Components.IN_BATTLE_ZONE, 1);
        data.set(entity, Components.OWNED_BY, data.get(source, Components.OWNED_BY));
    }
}
