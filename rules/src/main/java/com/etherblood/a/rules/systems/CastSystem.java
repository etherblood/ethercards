package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.casteffects.CastEffect;

import java.util.function.IntFunction;

public class CastSystem extends AbstractSystem {

    private final IntFunction<CardTemplate> cards;

    public CastSystem(IntFunction<CardTemplate> cards) {
        this.cards = cards;
    }

    @Override
    public void run(Game game, EntityData data) {
        IntList entities = data.list(Components.CAST_TARGET);
        for (int castSource : entities) {
            int cardTemplateId = data.get(castSource, Components.CARD_TEMPLATE);
            int target = data.get(castSource, Components.CAST_TARGET);
            CardTemplate template = cards.apply(cardTemplateId);
            int owner = data.get(castSource, Components.OWNED_BY);
            CardCast cast;
            if (data.get(owner, Components.ACTIVE_PLAYER_PHASE) == PlayerPhase.ATTACK_PHASE) {
                cast = template.getAttackPhaseCast();
            } else {
                cast = template.getBlockPhaseCast();
            }
            int manaCost = cast.getManaCost();
            if (manaCost != 0) {
                int mana = data.get(owner, Components.MANA);
                mana -= manaCost;
                if (mana < 0) {
                    throw new IllegalStateException();
                }
                data.set(owner, Components.MANA, mana);
            }
            for (CastEffect effect : cast.getEffects()) {
                effect.cast(game, data, castSource, target);
            }
            data.remove(castSource, Components.CAST_TARGET);
            data.remove(castSource, Components.IN_HAND_ZONE);
            data.remove(castSource, Components.CARD_TEMPLATE);
            data.remove(castSource, Components.OWNED_BY);
        }
    }
}
