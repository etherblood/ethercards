package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.collections.IntMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CardTemplateBuilder {

    protected Integer manaCost;
    protected final List<CardCastBuilder> casts = new ArrayList<>();
    protected final IntMap components = new IntMap();
    protected final Set<Tribe> tribes = EnumSet.noneOf(Tribe.class);
    protected final List<Effect> onCastEffects = new ArrayList<>();
    protected final List<Effect> onSummonEffects = new ArrayList<>();
    protected final List<Effect> onDeathEffects = new ArrayList<>();
    protected final List<Effect> onSurviveEffects = new ArrayList<>();
    protected final List<Effect> onUpkeepEffects = new ArrayList<>();
    protected final List<Effect> afterBattleEffects = new ArrayList<>();
    protected final List<Effect> onDrawEffects = new ArrayList<>();
    protected final Map<Integer, List<StatModifier>> componentModifiers = new HashMap<>();

    public CardCastBuilder newCast() {
        CardCastBuilder cast = new CardCastBuilder();
        casts.add(cast);
        return cast;
    }

    public void setManaCost(Integer manaCost) {
        this.manaCost = manaCost;
    }

    public void addTribe(Tribe tribe) {
        tribes.add(tribe);
    }

    public void onCast(Effect effect) {
        onCastEffects.add(effect);
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

    public void onDraw(Effect effect) {
        onDrawEffects.add(effect);
    }

    public void set(int component, int value) {
        components.set(component, value);
    }

    public void remove(int component) {
        components.remove(component);
    }

    public void modifyComponent(int component, StatModifier modifier) {
        componentModifiers.computeIfAbsent(component, x -> new ArrayList<>()).add(modifier);
    }

    public CardTemplate build(int id) {
        return new CardTemplate(id, !components.isEmpty(), manaCost, casts.stream().map(CardCastBuilder::build).toArray(CardCast[]::new), components, tribes, onCastEffects, onSummonEffects, onDeathEffects, onSurviveEffects, onUpkeepEffects, afterBattleEffects, onDrawEffects, componentModifiers);
    }

}
