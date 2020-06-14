package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.function.IntUnaryOperator;

public class OnDeathSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList deaths = data.list(core.DIE);
        for (int entity : deaths) {
            int sourceTemplateId = data.get(entity, core.MINION_TEMPLATE);
            MinionTemplate sourceTemplate = settings.templates.getMinion(sourceTemplateId);
            for (Effect effect : sourceTemplate.getOnDeathEffects()) {
                effect.apply(settings, data, random, entity, ~0);
            }
        }
    }

}
