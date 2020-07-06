package com.etherblood.a.rules.templates.instances.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class KolaghanDamageEffect implements Effect {

    public final int damage;

    public KolaghanDamageEffect(int damage) {
        this.damage = damage;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        int owner = data.get(source, core.OWNED_BY);
        int targetOwner = data.get(target, core.OWNED_BY);
        if (owner == targetOwner) {
            return;
        }
        int targetTemplate = data.get(target, core.CARD_TEMPLATE);
        for (int dead : data.list(core.IN_GRAVEYARD_ZONE)) {
            if (data.hasValue(dead, core.CARD_TEMPLATE, targetTemplate) && data.hasValue(dead, core.OWNED_BY, targetOwner)) {
                data.set(SystemsUtil.randomHero(data, random, targetOwner), core.DAMAGE_REQUEST, damage);
                break;
            }
        }
    }
}
