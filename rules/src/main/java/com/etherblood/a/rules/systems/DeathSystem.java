package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;

public class DeathSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        IntList deaths = data.list(Components.DIE);
        for (int entity : deaths) {
            data.remove(entity, Components.IN_BATTLE_ZONE);
            data.remove(entity, Components.OWNED_BY);
            data.remove(entity, Components.DIE);
            data.remove(entity, Components.ATTACKS_TARGET);
            data.remove(entity, Components.ATTACK);
            data.remove(entity, Components.HEALTH);
            data.remove(entity, Components.TIRED);
            // TODO: full cleanup
        }
    }

}
