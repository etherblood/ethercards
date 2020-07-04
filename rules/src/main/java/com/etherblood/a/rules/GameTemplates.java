package com.etherblood.a.rules;

import com.etherblood.a.rules.templates.CardTemplate;
import java.util.HashMap;
import java.util.Map;

public class GameTemplates {

    private final Map<Integer, CardTemplate> cards;

    public GameTemplates(Map<Integer, ? extends CardTemplate> cards) {
        this.cards = new HashMap<>(cards);
    }

    public CardTemplate getCard(int card) {
        return cards.get(card);
    }
}
