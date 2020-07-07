package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Modifier;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class SurvivalSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final Modifier[] modifiers;
    private final Trigger[] triggers;

    public SurvivalSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.modifiers = new Modifier[]{
            (entity, value) -> data.has(entity, core.IN_BATTLE_ZONE) ? value : 0,
            (entity, value) -> data.has(entity, core.DEATH_ACTION) ? 0 : value
        };
        this.triggers = new Trigger[]{
            (entity, value) -> {
                int templateId = data.get(entity, core.CARD_TEMPLATE);
                CardTemplate template = templates.getCard(templateId);
                for (Effect onSurviveEffect : template.getOnSelfSurviveEffects()) {
                    onSurviveEffect.apply(data, templates, random, events, entity, ~0);
                }
            }
        };
    }

    @Override
    public boolean isActive() {
        return data.list(core.DAMAGE_SURVIVAL_REQUEST).nonEmpty();
    }

    @Override
    public void before() {
        for (int entity : data.list(core.DAMAGE_SURVIVAL_REQUEST)) {
            int survival = data.get(entity, core.DAMAGE_SURVIVAL_REQUEST);
            for (int i = 0; survival > 0 && i < modifiers.length; i++) {
                survival = modifiers[i].modify(entity, survival);
            }
            if (survival > 0) {
                data.set(entity, core.DAMAGE_SURVIVAL_ACTION, survival);
            }
            data.remove(entity, core.DAMAGE_SURVIVAL_REQUEST);
        }
    }

    @Override
    public void run() {
        for (int entity : data.list(core.DAMAGE_SURVIVAL_ACTION)) {
            int survival = data.get(entity, core.DAMAGE_SURVIVAL_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(entity, survival);
            }
        }
    }

    @Override
    public void after() {
        for (int entity : data.list(core.DAMAGE_SURVIVAL_ACTION)) {
            data.remove(entity, core.DAMAGE_SURVIVAL_ACTION);
        }
    }
}
