package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DamageEvent;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.function.IntUnaryOperator;

public class DamageSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener eventListener) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList entities = data.list(core.DAMAGE_ACTION);
        for (int entity : entities) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                data.getOptional(entity, core.HEALTH).ifPresent(health -> {
                    int damage = data.get(entity, core.DAMAGE_ACTION);
                    health -= damage;
                    data.set(entity, core.HEALTH, health);
                    if (health <= 0) {
                        data.set(entity, core.DEATH_REQUEST, 1);
                    } else {
                        int sourceTemplateId = data.get(entity, core.MINION_TEMPLATE);
                        MinionTemplate sourceTemplate = settings.templates.getMinion(sourceTemplateId);
                        onSurvive(settings, data, random, eventListener, entity, sourceTemplate);
                    }
                    eventListener.fire(new DamageEvent(entity, damage));
                });
            }
        }
    }

    private void onSurvive(GameSettings settings, EntityData data, IntUnaryOperator random, GameEventListener eventListener, int entity, MinionTemplate template) {
        for (Effect effect : template.getOnSurviveEffects()) {
            effect.apply(settings, data, random, eventListener, entity, ~0);
        }
    }
}
