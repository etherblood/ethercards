package com.etherblood.a.rules;

import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.IntFunction;

public class GameSettings {

    public int playerCount = 2;
    public IntFunction<CardTemplate> cards;
    public IntFunction<MinionTemplate> minions;
    public Random random = new SecureRandom();
    public boolean backupsEnabled = true;

}
