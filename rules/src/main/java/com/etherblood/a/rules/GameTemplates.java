package com.etherblood.a.rules;

import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.HashMap;
import java.util.Map;

public class GameTemplates {

    private final Map<Integer, CardTemplate> cards;
    private final Map<Integer, MinionTemplate> minions;

    public GameTemplates(Map<Integer, ? extends CardTemplate> cards, Map<Integer, ? extends MinionTemplate> minions) {
        this.cards = new HashMap<>(cards);
        this.minions = new HashMap<>(minions);
    }

    public CardTemplate getCard(int card) {
        return cards.get(card);
    }

    public MinionTemplate getMinion(int minion) {
        return minions.get(minion);
    }
}
