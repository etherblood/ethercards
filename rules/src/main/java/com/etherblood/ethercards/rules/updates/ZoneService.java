package com.etherblood.ethercards.rules.updates;

import com.etherblood.ethercards.entities.ComponentMeta;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.ZoneState;
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
        CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
        initComponents(entity, template.getLibrary());
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
        CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
        initComponents(entity, template.getHand());
        data.set(entity, core.IN_HAND_ZONE, data.createEntity());
        triggerService.initEffects(entity);
    }

    public void removeFromHand(int entity) {
        assert data.has(entity, core.IN_HAND_ZONE);
        triggerService.cleanupEffects(entity);
        data.remove(entity, core.IN_HAND_ZONE);
        clearZoneComponents(entity);
    }

    public void addToGraveyard(int entity) {
        assertNoZone(entity);
        CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
        initComponents(entity, template.getGraveyard());
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
            initComponents(entity, template.getBattle());
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
        clearZoneComponents(entity);
    }

    private void clearZoneComponents(int entity) {
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

    public void initComponents(int card) {
        CardTemplate template = templates.getCard(data.get(card, core.CARD_TEMPLATE));
        ZoneState zone = template.getActiveZone(card, data);
        initComponents(card, zone);
    }

    private void initComponents(int card, ZoneState zone) {
        IntMap components = zone.getComponents();
        for (int component : components) {
            data.set(card, component, components.get(component));
        }
    }
}
