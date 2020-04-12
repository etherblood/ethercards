package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.casteffects.Effect;

public class OnDeathSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList deaths = data.list(core.DIE);
        for (int entity : deaths) {
            int sourceTemplateId = data.get(entity, core.MINION_TEMPLATE);
            MinionTemplate sourceTemplate = game.getMinions().apply(sourceTemplateId);
            for (Effect effect : sourceTemplate.getOnDeathEffects()) {
                effect.apply(game, data, entity, ~0);
            }
        }
    }

}
