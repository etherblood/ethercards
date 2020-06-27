package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.effects.Effect;
import com.etherblood.a.rules.updates.ActionSystem;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public class CastSystem implements ActionSystem {

    private final EntityData data;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final CoreComponents core;

    public CastSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.templates = templates;
        this.random = random;
        this.events = events;
        this.core = data.getComponents().getModule(CoreComponents.class);
    }

    @Override
    public boolean isActive() {
        return data.list(core.CAST_TARGET).nonEmpty();
    }

    @Override
    public void modify() {
    }

    @Override
    public void triggerAndClean() {
    }

    @Override
    public void apply() {
        for (int castSource : data.list(core.CAST_TARGET)) {
            int cardTemplateId = data.get(castSource, core.CARD_TEMPLATE);
            int target = data.get(castSource, core.CAST_TARGET);
            CardTemplate template = templates.getCard(cardTemplateId);
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
                effect.apply(data, templates, random, events, castSource, target);
            }
            data.remove(castSource, core.CAST_TARGET);
            data.remove(castSource, core.IN_HAND_ZONE);
            data.remove(castSource, core.CARD_TEMPLATE);
            data.remove(castSource, core.OWNED_BY);
        }
    }
}
