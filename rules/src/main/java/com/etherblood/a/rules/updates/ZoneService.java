package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.function.IntUnaryOperator;

public class ZoneService {

    private final EntityData data;
    private final GameTemplates templates;
    private final CoreComponents core;
    private final TriggerService triggerService;

    public ZoneService(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.triggerService = new TriggerService(data, templates, random, events);
    }
    
    public void addToLibrary(int entity) {
        assertNoZone(entity);
        data.set(entity, core.IN_LIBRARY_ZONE, data.createEntity());
        triggerService.initEffects(entity);
    }

    public void removeFromLibrary(int entity) {
        assert data.has(entity, core.IN_LIBRARY_ZONE);
        triggerService.cleanupEffects(entity);
        data.remove(entity, core.IN_LIBRARY_ZONE);
    }

    public void addToHand(int entity) {
        assertNoZone(entity);
        data.set(entity, core.IN_HAND_ZONE, data.createEntity());
        triggerService.initEffects(entity);
    }

    public void removeFromHand(int entity) {
        assert data.has(entity, core.IN_HAND_ZONE);
        triggerService.cleanupEffects(entity);
        data.remove(entity, core.IN_HAND_ZONE);
    }

    public void addToGraveyard(int entity) {
        assertNoZone(entity);
        data.set(entity, core.IN_GRAVEYARD_ZONE, data.createEntity());
        triggerService.initEffects(entity);
        
        triggerService.onEnterGraveyard(entity);
    }

    public void removeFromGraveyard(int entity) {
        assert data.has(entity, core.IN_GRAVEYARD_ZONE);
        triggerService.cleanupEffects(entity);
        data.remove(entity, core.IN_GRAVEYARD_ZONE);
    }

    public void addToBattle(int entity, boolean applyComponents) {
        assertNoZone(entity);
        if (applyComponents) {
            CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
            for (int component : template.components()) {
                data.set(entity, component, template.get(component));
            }
        }
        data.set(entity, core.IN_BATTLE_ZONE, data.createEntity());
        triggerService.initEffects(entity);
        
        triggerService.onEnterBattle(entity);
    }

    public void removeFromBattle(int entity) {
        assert data.has(entity, core.IN_BATTLE_ZONE);
        triggerService.cleanupEffects(entity);
        data.remove(entity, core.IN_BATTLE_ZONE);
        data.getOptional(entity, core.ORIGINAL_CARD_TEMPLATE).ifPresent(template -> {
            data.remove(entity, core.ORIGINAL_CARD_TEMPLATE);
            data.set(entity, core.CARD_TEMPLATE, template);
        });
        //TODO: use a whitelist instead?
        IntList blacklist = new IntList(core.CARD_TEMPLATE, core.OWNER, core.TEAM, core.HERO);
        for (ComponentMeta meta : data.getComponents().getMetas()) {
            if (blacklist.contains(meta.id)) {
                continue;
            }
            data.remove(entity, meta.id);
        }
    }

    private void assertNoZone(int entity) {
        assert !data.has(entity, core.IN_BATTLE_ZONE);
        assert !data.has(entity, core.IN_HAND_ZONE);
        assert !data.has(entity, core.IN_LIBRARY_ZONE);
        assert !data.has(entity, core.IN_GRAVEYARD_ZONE);
    }

}
