package com.etherblood.a.rules;

import com.etherblood.a.entities.ComponentsModule;
import java.util.function.ToIntFunction;

public class CoreComponents implements ComponentsModule {

    public final int HAS_LOST;
    public final int HAS_WON;
    public final int PLAYER_INDEX;
    public final int ACTIVE_PLAYER_PHASE;
    public final int END_PHASE;
    public final int HEALTH;
    public final int ATTACK;
    public final int DAMAGE;
    public final int DIE;
    public final int OWNED_BY;
    public final int IN_BATTLE_ZONE;
    public final int IN_HAND_ZONE;
    public final int IN_LIBRARY_ZONE;
    public final int BLOCKS_ATTACKER;
    public final int CANNOT_ATTACK;
    public final int CANNOT_BLOCK;
    public final int CANNOT_BE_BLOCKED;
    public final int ATTACKS_TARGET;
    public final int CAST_TARGET;
    public final int MANA;
    public final int MANA_POOL;
    public final int MANA_GROWTH;
    public final int DRAW_CARDS;
    public final int DRAWS_PER_TURN;
    public final int DRAWS_ON_ATTACK;
    public final int DRAWS_ON_BLOCK;
    public final int DRAWS_ON_ATTACKED;
    public final int FATIGUE;
    public final int TIRED;
    public final int SUMMONING_SICKNESS;
    public final int CARD_TEMPLATE;
    public final int MINION_TEMPLATE;
    public final int HERO;
    public final int MULLIGAN;


    public CoreComponents(ToIntFunction<String> register) {
        HAS_LOST = register.applyAsInt("HAS_LOST");
        HAS_WON = register.applyAsInt("HAS_WON");
        PLAYER_INDEX = register.applyAsInt("PLAYER_INDEX");
        ACTIVE_PLAYER_PHASE = register.applyAsInt("ACTIVE_PLAYER_PHASE");
        END_PHASE = register.applyAsInt("END_PHASE");
        HEALTH = register.applyAsInt("HEALTH");
        ATTACK = register.applyAsInt("ATTACK");
        DAMAGE = register.applyAsInt("DAMAGE");
        DIE = register.applyAsInt("DIE");
        OWNED_BY = register.applyAsInt("OWNED_BY");
        IN_BATTLE_ZONE = register.applyAsInt("IN_BATTLE_ZONE");
        IN_HAND_ZONE = register.applyAsInt("IN_HAND_ZONE");
        IN_LIBRARY_ZONE = register.applyAsInt("IN_LIBRARY_ZONE");
        CANNOT_ATTACK = register.applyAsInt("CANNOT_ATTACK");
        CANNOT_BLOCK = register.applyAsInt("CANNOT_BLOCK");
        CANNOT_BE_BLOCKED = register.applyAsInt("CANNOT_BE_BLOCKED");
        BLOCKS_ATTACKER = register.applyAsInt("BLOCKS_ATTACKER");
        ATTACKS_TARGET = register.applyAsInt("ATTACKS_PLAYER");
        CAST_TARGET = register.applyAsInt("CAST_TARGET");
        MANA = register.applyAsInt("MANA");
        MANA_POOL = register.applyAsInt("MANA_POOL");
        MANA_GROWTH = register.applyAsInt("MANA_GROWTH");
        DRAW_CARDS = register.applyAsInt("DRAW_CARDS");
        DRAWS_PER_TURN = register.applyAsInt("DRAWS_PER_TURN");
        DRAWS_ON_ATTACK = register.applyAsInt("DRAWS_ON_ATTACK");
        DRAWS_ON_BLOCK = register.applyAsInt("DRAWS_ON_BLOCK");
        DRAWS_ON_ATTACKED = register.applyAsInt("DRAWS_ON_ATTACKED");
        FATIGUE = register.applyAsInt("FATIGUE");
        TIRED = register.applyAsInt("TIRED");
        SUMMONING_SICKNESS = register.applyAsInt("SUMMONING_SICKNESS");
        CARD_TEMPLATE = register.applyAsInt("CARD_TEMPLATE");
        MINION_TEMPLATE = register.applyAsInt("MINION_TEMPLATE");
        HERO = register.applyAsInt("HERO");
        MULLIGAN = register.applyAsInt("MULLIGAN");
    }
}
