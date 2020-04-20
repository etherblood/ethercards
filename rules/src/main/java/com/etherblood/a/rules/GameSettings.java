package com.etherblood.a.rules;

import com.etherblood.a.entities.Components;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class GameSettings {

    public final int playerCount;
    public final IntFunction<CardTemplate> cards;
    public final IntFunction<MinionTemplate> minions;
    public final IntUnaryOperator random;
    public final boolean backupsEnabled;
    public final boolean validateMoves;
    public final Components components;
    public final List<AbstractSystem> generalSystems;

    public GameSettings(int playerCount, IntFunction<CardTemplate> cards, IntFunction<MinionTemplate> minions, IntUnaryOperator random, boolean backupsEnabled, boolean validateMoves, Components components, List<AbstractSystem> generalSystems) {
        this.playerCount = playerCount;
        this.cards = cards;
        this.minions = minions;
        this.random = random;
        this.backupsEnabled = backupsEnabled;
        this.validateMoves = validateMoves;
        this.components = components;
        this.generalSystems = Collections.unmodifiableList(new ArrayList<>(generalSystems));
    }

}
