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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.IntFunction;

public class CastSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(CastSystem.class);

    private final IntFunction<CardTemplate> cards;
    private final IntFunction<MinionTemplate> minions;

    public CastSystem(IntFunction<CardTemplate> cards, IntFunction<MinionTemplate> minions) {
        this.cards = cards;
        this.minions = minions;
    }

    @Override
    public void run(EntityData data, Random random) {
        IntList entities = data.list(Components.CAST_TARGET);
        for (int casting : entities) {
            int cardTemplateId = data.get(casting, Components.CARD_TEMPLATE);
            int target = data.get(casting, Components.CAST_TARGET);
            CardTemplate template = cards.apply(cardTemplateId);
            int owner = data.get(casting, Components.OWNED_BY);
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
                LOG.info("{} paid {} mana and has {} now.",
                        entityLog(owner),
                        manaCost,
                        componentLog(Components.MANA, mana));
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
                    LOG.debug("{} summoned {}.",
                            entityLog(casting),
                            entityLog(entity));
                } else if (effect instanceof SingleTargetDamageEffect) {
                    int damage = ((SingleTargetDamageEffect) effect).damage;
                    SystemsUtil.damage(data, target, damage);
                } else {
                    LOG.error("Effect type " + effect.getClass().getSimpleName() + " not supported.");
                }
            }
            data.remove(casting, Components.CAST_TARGET);
            data.remove(casting, Components.IN_HAND_ZONE);
            data.remove(casting, Components.CARD_TEMPLATE);
            data.remove(casting, Components.OWNED_BY);
            LOG.debug("{} removed from {}.",
                    entityLog(casting),
                    componentLog(Components.IN_HAND_ZONE, 1));
            LOG.info("{} cast {}.", entityLog(owner), entityLog(casting));
        }
    }
}
