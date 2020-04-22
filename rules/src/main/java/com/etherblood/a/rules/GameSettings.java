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

    public final IntFunction<CardTemplate> cards;
    public final IntFunction<MinionTemplate> minions;
    public final IntUnaryOperator random;
    public final Components components;
    public final List<AbstractSystem> generalSystems;
    
    //TODO: toggle values below are implementation details and do not belong here
    public final boolean backupsEnabled;
    public final boolean validateMoves;

    public GameSettings(IntFunction<CardTemplate> cards, IntFunction<MinionTemplate> minions, IntUnaryOperator random, boolean backupsEnabled, boolean validateMoves, Components components, List<AbstractSystem> generalSystems) {
        this.cards = cards;
        this.minions = minions;
        this.random = random;
        this.backupsEnabled = backupsEnabled;
        this.validateMoves = validateMoves;
        this.components = components;
        this.generalSystems = Collections.unmodifiableList(new ArrayList<>(generalSystems));
    }

}
