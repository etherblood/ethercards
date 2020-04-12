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
import java.util.Random;
import java.util.function.IntFunction;

public class GameSettingsBuilder {

    public int playerCount = 2;
    public IntFunction<CardTemplate> cards;
    public IntFunction<MinionTemplate> minions;
    public Random random = new SecureRandom();
    public boolean backupsEnabled = true;
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

    public GameSettings build() {
        return new GameSettings(playerCount, cards, minions, random, backupsEnabled, components, generalSystems);
    }

}
