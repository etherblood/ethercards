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
    protected TargetSelection castTarget;
    protected final IntMap components = new IntMap();
    protected final List<Effect> castEffects = new ArrayList<>();
    protected final Set<Tribe> tribes = EnumSet.noneOf(Tribe.class);
    protected final Map<Integer, List<Effect>> inBattle = new HashMap<>();
    protected final Map<Integer, List<Effect>> inHand = new HashMap<>();
    protected final Map<Integer, List<Effect>> inGraveyard = new HashMap<>();
    protected final Map<Integer, List<StatModifier>> componentModifiers = new HashMap<>();

    public void setCastTarget(TargetSelection castTarget) {
        this.castTarget = castTarget;
    }

    public void castEffect(Effect effect) {
        castEffects.add(effect);
    }

    public void setManaCost(Integer manaCost) {
        this.manaCost = manaCost;
    }

    public void addTribe(Tribe tribe) {
        tribes.add(tribe);
    }

    public void inBattle(int triggerComponent, Effect effect) {
        inBattle.computeIfAbsent(triggerComponent, x -> new ArrayList<>()).add(effect);
    }

    public void inHand(int triggerComponent, Effect effect) {
        inHand.computeIfAbsent(triggerComponent, x -> new ArrayList<>()).add(effect);
    }

    public void inGraveyard(int triggerComponent, Effect effect) {
        inGraveyard.computeIfAbsent(triggerComponent, x -> new ArrayList<>()).add(effect);
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
        return new CardTemplate(id, !components.isEmpty(), manaCost, castTarget, castEffects, components, tribes, inBattle, inHand, inGraveyard, componentModifiers);
    }

}
