package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;

public class ZoneService {

    private final EntityData data;
    private final GameTemplates templates;
    private final CoreComponents core;

    public ZoneService(EntityData data, GameTemplates templates) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
    }

    public void addToGraveyard(int entity) {
        assert !data.has(entity, core.IN_GRAVEYARD_ZONE);
        data.set(entity, core.IN_GRAVEYARD_ZONE, data.createEntity());
    }

    public void removeFromGraveyard(int entity) {
        assert data.has(entity, core.IN_GRAVEYARD_ZONE);
        data.remove(entity, core.IN_GRAVEYARD_ZONE);
    }

    public void addToBattle(int entity) {
        assert !data.has(entity, core.IN_BATTLE_ZONE);
        CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
        for (int component : template.components()) {
            data.set(entity, component, template.get(component));
        }
        data.set(entity, core.IN_BATTLE_ZONE, data.createEntity());
    }

    public void removeFromBattle(int entity) {
        assert data.has(entity, core.IN_BATTLE_ZONE);
        data.remove(entity, core.IN_BATTLE_ZONE);
        //TODO: use a blacklist instead
        IntList whiteList = new IntList(core.CARD_TEMPLATE, core.OWNED_BY);
        for (ComponentMeta meta : data.getComponents().getMetas()) {
            if (whiteList.contains(meta.id)) {
                continue;
            }
            data.remove(entity, meta.id);
        }
    }

}
