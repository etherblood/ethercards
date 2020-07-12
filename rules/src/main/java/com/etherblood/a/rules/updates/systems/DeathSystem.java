package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DeathEvent;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.ZoneService;
import com.etherblood.a.rules.updates.Modifier;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class DeathSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final Modifier[] modifiers;
    private final Trigger[] triggers;

    public DeathSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.modifiers = new Modifier[]{
            (entity, value) -> data.has(entity, core.IN_BATTLE_ZONE) && !data.has(entity, core.INDESTRUCTIBLE) ? value : 0
        };
        this.triggers = new Trigger[]{
            (entity, value) -> {
                if (data.has(entity, core.HERO)) {
                    int owner = data.get(entity, core.OWNER);
                    data.set(owner, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
                }

                int templateId = data.get(entity, core.CARD_TEMPLATE);
                CardTemplate template = templates.getCard(templateId);
                for (Effect onDeathEffect : template.getOnSelfDeathEffects()) {
                    onDeathEffect.apply(data, templates, random, events, entity, entity);
                }

                events.fire(new DeathEvent(entity));
            }
        };
    }

    @Override
    public boolean isActive() {
        return data.list(core.DEATH_REQUEST).nonEmpty();
    }

    @Override
    public void before() {
        for (int entity : data.list(core.DEATH_REQUEST)) {
            int death = data.get(entity, core.DEATH_REQUEST);
            for (int i = 0; death > 0 && i < modifiers.length; i++) {
                death = modifiers[i].modify(entity, death);
            }
            if (death > 0) {
                data.set(entity, core.DEATH_ACTION, death);
            }
            data.remove(entity, core.DEATH_REQUEST);
        }
    }

    @Override
    public void run() {
        for (int entity : data.list(core.DEATH_ACTION)) {
            int death = data.get(entity, core.DEATH_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(entity, death);
            }
        }
    }

    @Override
    public void after() {
        ZoneService zoneService = new ZoneService(data, templates);
        for (int entity : data.list(core.DEATH_ACTION)) {
            data.remove(entity, core.DEATH_ACTION);
            zoneService.removeFromBattle(entity);
            zoneService.addToGraveyard(entity);
        }
    }
}
