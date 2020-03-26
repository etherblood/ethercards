package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.casteffects.CastEffect;
import com.etherblood.a.rules.templates.casteffects.SingleTargetDamageEffect;
import com.etherblood.a.rules.templates.casteffects.SummonEffect;

import java.util.Random;
import java.util.function.IntFunction;

public class CastSystem extends AbstractSystem {

    private final IntFunction<CardTemplate> cards;
    private final IntFunction<MinionTemplate> minions;

    public CastSystem(IntFunction<CardTemplate> cards, IntFunction<MinionTemplate> minions) {
        this.cards = cards;
        this.minions = minions;
    }

    @Override
    public void run(EntityData data, Random random) {
        IntList entities = data.list(Components.CAST_TARGET);
        for (int castSource : entities) {
            int cardTemplateId = data.get(castSource, Components.CARD_TEMPLATE);
            int target = data.get(castSource, Components.CAST_TARGET);
            CardTemplate template = cards.apply(cardTemplateId);
            int owner = data.get(castSource, Components.OWNED_BY);
            CardCast cast;
            if (data.has(owner, Components.IN_ATTACK_PHASE)) {
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
                if (effect instanceof SummonEffect) {
                    int minionTemplate = ((SummonEffect) effect).minionId;
                    MinionTemplate minion = minions.apply(minionTemplate);
                    int entity = data.createEntity();
                    for (int component : minion) {
                        data.set(entity, component, minion.get(component));
                    }
                    data.set(entity, Components.IN_BATTLE_ZONE, 1);
                    data.set(entity, Components.OWNED_BY, owner);
                } else if (effect instanceof SingleTargetDamageEffect) {
                    int damage = ((SingleTargetDamageEffect) effect).damage;
                    SystemsUtil.damage(data, target, damage);
                } else {
                    throw new UnsupportedOperationException("Effect type " + effect.getClass().getSimpleName() + " not supported.");
                }
            }
            data.remove(castSource, Components.CAST_TARGET);
            data.remove(castSource, Components.IN_HAND_ZONE);
            data.remove(castSource, Components.CARD_TEMPLATE);
            data.remove(castSource, Components.OWNED_BY);
        }
    }
}
