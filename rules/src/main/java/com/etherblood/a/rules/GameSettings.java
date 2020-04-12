package com.etherblood.a.rules;

import com.etherblood.a.entities.Components;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;

public class GameSettings {

    public final int playerCount;
    public final IntFunction<CardTemplate> cards;
    public final IntFunction<MinionTemplate> minions;
    public final Random random;
    public final boolean backupsEnabled;
    public final Components components;
    public final List<AbstractSystem> generalSystems;

    public GameSettings(int playerCount, IntFunction<CardTemplate> cards, IntFunction<MinionTemplate> minions, Random random, boolean backupsEnabled, Components components, List<AbstractSystem> generalSystems) {
        this.playerCount = playerCount;
        this.cards = cards;
        this.minions = minions;
        this.random = random;
        this.backupsEnabled = backupsEnabled;
        this.components = components;
        this.generalSystems = Collections.unmodifiableList(new ArrayList<>(generalSystems));
    }

}
