package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.game.events.api.events.ParticleEvent;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.SystemsUtil;

import java.util.function.IntUnaryOperator;

public class KolaghanDamageEffect implements Effect {

    public final int damage;

    public KolaghanDamageEffect(int damage) {
        this.damage = damage;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getSchema().getModule(CoreComponents.class);

        int owner = data.get(source, core.OWNER);
        int targetOwner = data.get(target, core.OWNER);
        if (owner == targetOwner) {
            return;
        }
        int targetTemplate = data.get(target, core.CARD_TEMPLATE);
        for (int dead : data.list(core.IN_GRAVEYARD_ZONE)) {
            if (data.hasValue(dead, core.CARD_TEMPLATE, targetTemplate) && data.hasValue(dead, core.OWNER, targetOwner)) {
                int targetHero = SystemsUtil.heroOf(data, targetOwner);
                SystemsUtil.damage(data, events, targetHero, damage);
                events.fire(new ParticleEvent("kolaghanDamage", source, targetHero));
                break;
            }
        }
    }
}
