package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.function.IntUnaryOperator;

public class CastSystem extends AbstractSystem {

    @Override
    public void run(GameSettings settings, EntityData data, IntUnaryOperator random) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList entities = data.list(core.CAST_TARGET);
        for (int castSource : entities) {
            int cardTemplateId = data.get(castSource, core.CARD_TEMPLATE);
            int target = data.get(castSource, core.CAST_TARGET);
            CardTemplate template = settings.templates.getCard(cardTemplateId);
            int owner = data.get(castSource, core.OWNED_BY);
            CardCast cast;
            if (data.get(owner, core.ACTIVE_PLAYER_PHASE) == PlayerPhase.ATTACK) {
                cast = template.getAttackPhaseCast();
            } else {
                cast = template.getBlockPhaseCast();
            }
            int manaCost = cast.getManaCost();
            if (manaCost != 0) {
                int mana = data.get(owner, core.MANA);
                mana -= manaCost;
                if (mana < 0) {
                    throw new IllegalStateException();
                }
                data.set(owner, core.MANA, mana);
            }
            for (Effect effect : cast.getEffects()) {
                effect.apply(settings, data, random, castSource, target);
            }
            data.remove(castSource, core.CAST_TARGET);
            data.remove(castSource, core.IN_HAND_ZONE);
            data.remove(castSource, core.CARD_TEMPLATE);
            data.remove(castSource, core.OWNED_BY);
        }
    }
}
