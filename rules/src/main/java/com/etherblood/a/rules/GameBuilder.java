package com.etherblood.a.rules;

import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.IntFunction;

public class GameBuilder {

    private IntFunction<CardTemplate> cards;
    private IntFunction<MinionTemplate> minions;
    private Random random = new SecureRandom();
    private boolean backupsEnabled = true;

    public void setCards(IntFunction<CardTemplate> cards) {
        this.cards = cards;
    }

    public void setMinions(IntFunction<MinionTemplate> minions) {
        this.minions = minions;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setBackupsEnabled(boolean backupsEnabled) {
        this.backupsEnabled = backupsEnabled;
    }

    public Game build() {
        return new Game(random, cards, minions, backupsEnabled);
    }
}
