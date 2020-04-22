package com.etherblood.a.rules;

import com.etherblood.a.entities.Components;
import com.etherblood.a.rules.systems.DamageSystem;
import com.etherblood.a.rules.systems.DeathSystem;
import com.etherblood.a.rules.systems.DrawSystem;
import com.etherblood.a.rules.systems.PlayerStatusSystem;
import com.etherblood.a.rules.systems.OnDeathSystem;
import com.etherblood.a.rules.systems.OnSurvivalSystem;
import com.etherblood.a.rules.systems.TemporariesCleanupSystem;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class GameSettingsBuilder {

    public IntFunction<CardTemplate> cards;
    public IntFunction<MinionTemplate> minions;
    public IntUnaryOperator random = new SecureRandom()::nextInt;
    public boolean backupsEnabled = true;
    public boolean validateMoves = true;
    public Components components;
    public List<AbstractSystem> generalSystems = Arrays.asList(
            new DrawSystem(),
            new DamageSystem(),
            new OnDeathSystem(),
            new OnSurvivalSystem(),
            new DeathSystem(),
            new PlayerStatusSystem(),
            new TemporariesCleanupSystem()
    );

    public GameSettingsBuilder() {
    }

    public GameSettingsBuilder(GameSettings settings) {
        cards = settings.cards;
        minions = settings.minions;
        random = settings.random;
        backupsEnabled = settings.backupsEnabled;
        validateMoves = settings.validateMoves;
        components = settings.components;
        generalSystems = settings.generalSystems;
    }

    public GameSettings build() {
        return new GameSettings(cards, minions, random, backupsEnabled, validateMoves, components, generalSystems);
    }

}
