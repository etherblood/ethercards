package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.rules.templates.CardTemplate;
import java.util.Map;

public class GameTemplates {

    private final CardTemplate[] cards;

    public GameTemplates(Map<Integer, ? extends CardTemplate> cards) {
        this.cards = new CardTemplate[cards.size()];
        for (Map.Entry<Integer, ? extends CardTemplate> entry : cards.entrySet()) {
            this.cards[entry.getKey()] = entry.getValue();
        }
    }

    public CardTemplate getCard(int id) {
        return cards[id];
    }
}
