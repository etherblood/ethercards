package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class DeathSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        IntList deaths = data.list(Components.DIE);
        for (int entity : deaths) {
            data.getOptional(entity, Components.SUMMON_ON_DEATH).ifPresent(template -> {
                SystemsUtil.summon(game, template, data.get(entity, Components.OWNED_BY));
                data.remove(entity, Components.SUMMON_ON_DEATH);
            });
            data.remove(entity, Components.IN_BATTLE_ZONE);
            data.remove(entity, Components.OWNED_BY);
            data.remove(entity, Components.DIE);
            data.remove(entity, Components.ATTACKS_TARGET);
            data.remove(entity, Components.ATTACK);
            data.remove(entity, Components.HEALTH);
            data.remove(entity, Components.TIRED);
            data.remove(entity, Components.HERO);
            // TODO: full cleanup
            
            for (int attacker : data.list(Components.ATTACKS_TARGET)) {
                int target = data.get(attacker, Components.ATTACKS_TARGET);
                if (target == entity) {
                    data.remove(attacker, Components.ATTACKS_TARGET);
                }
            }
        }
    }

}
