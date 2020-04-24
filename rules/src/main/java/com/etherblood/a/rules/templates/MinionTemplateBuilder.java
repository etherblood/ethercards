package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.List;

public class MinionTemplateBuilder {

    protected final CoreComponents core;
    protected final IntMap components = new IntMap();
    protected final List<Effect> onDeathEffects = new ArrayList<>();
    protected final List<Effect> onSurviveEffects = new ArrayList<>();

    public MinionTemplateBuilder(CoreComponents core) {
        this.core = core;
    }
    
    public void onDeath(Effect effect) {
        onDeathEffects.add(effect);
    }
    
    public void onSurvive(Effect effect) {
        onSurviveEffects.add(effect);
    }

    public void set(int component, int value) {
        components.set(component, value);
    }

    public void remove(int component) {
        components.remove(component);
    }

    public MinionTemplate build(int id) {
        components.set(core.MINION_TEMPLATE, id);
        MinionTemplate minionTemplate = new MinionTemplate(id, components, onDeathEffects, onSurviveEffects);
        return minionTemplate;
    }

}
