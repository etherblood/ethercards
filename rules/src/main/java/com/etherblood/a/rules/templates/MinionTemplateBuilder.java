package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MinionTemplateBuilder {

    protected final CoreComponents core;
    protected final IntMap components = new IntMap();
    protected final Set<Tribe> tribes = EnumSet.noneOf(Tribe.class);
    protected final List<Effect> onSummonEffects = new ArrayList<>();
    protected final List<Effect> onDeathEffects = new ArrayList<>();
    protected final List<Effect> onSurviveEffects = new ArrayList<>();
    protected final List<Effect> onUpkeepEffects = new ArrayList<>();
    protected final List<Effect> afterBattleEffects = new ArrayList<>();

    public MinionTemplateBuilder(CoreComponents core) {
        this.core = core;
    }

    public void withTribe(Tribe tribe) {
        tribes.add(tribe);
    }

    public void onSummon(Effect effect) {
        onSummonEffects.add(effect);
    }

    public void onDeath(Effect effect) {
        onDeathEffects.add(effect);
    }

    public void onSurvive(Effect effect) {
        onSurviveEffects.add(effect);
    }

    public void onUpkeep(Effect effect) {
        onUpkeepEffects.add(effect);
    }

    public void afterBattle(Effect effect) {
        afterBattleEffects.add(effect);
    }

    public void set(int component, int value) {
        components.set(component, value);
    }

    public void remove(int component) {
        components.remove(component);
    }

    public MinionTemplate build(int id) {
        components.set(core.MINION_TEMPLATE, id);
        return new MinionTemplate(id, components, tribes, onSummonEffects, onDeathEffects, onSurviveEffects, onUpkeepEffects, afterBattleEffects);
    }

}
