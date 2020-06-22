package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DeathEvent;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import java.util.function.IntUnaryOperator;

public class DeathSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener eventListener) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList deaths = data.list(core.DEATH_ACTION);
        for (int entity : deaths) {
            eventListener.fire(new DeathEvent(entity));
            data.remove(entity, core.OWNED_BY);
            data.remove(entity, core.ATTACK);
            data.remove(entity, core.HEALTH);
            data.remove(entity, core.TIRED);
            data.remove(entity, core.SUMMONING_SICKNESS);
            data.remove(entity, core.HERO);
            data.remove(entity, core.POISONED);
            data.remove(entity, core.VENOM);
            data.remove(entity, core.TRAMPLE);
            data.remove(entity, core.VIGILANCE);
            data.remove(entity, core.LIFELINK);
            data.remove(entity, core.MINION_TEMPLATE);

            data.remove(entity, core.DEATH_REQUEST);
            data.remove(entity, core.DEATH_ACTION);
            data.remove(entity, core.DAMAGE_REQUEST);
            data.remove(entity, core.DAMAGE_ACTION);

            data.remove(entity, core.IN_BATTLE_ZONE);
            data.remove(entity, core.MANA_POOL);
            data.remove(entity, core.MANA_GROWTH);
            data.remove(entity, core.DRAWS_PER_TURN);
            data.remove(entity, core.DRAWS_ON_ATTACK);
            data.remove(entity, core.DRAWS_ON_BLOCK);
            data.remove(entity, core.DRAWS_ON_ATTACKED);
            data.remove(entity, core.ATTACKS_TARGET);
            data.remove(entity, core.OWN_MINIONS_HASTE_AURA);
            // TODO: remove all interacting components

        }
    }

}
