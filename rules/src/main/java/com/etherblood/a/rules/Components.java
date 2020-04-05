package com.etherblood.a.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Components {

    private static final List<ComponentMeta> COMPONENTS = new ArrayList<>();

    public static final int HAS_LOST;
    public static final int HAS_WON;
    public static final int PLAYER_INDEX;
    public static final int ACTIVE_PLAYER_PHASE;
    public static final int END_BLOCK_PHASE;
    public static final int END_ATTACK_PHASE;
    public static final int HEALTH;
    public static final int ATTACK;
    public static final int DAMAGE;
    public static final int DIE;
    public static final int OWNED_BY;
    public static final int IN_BATTLE_ZONE;
    public static final int IN_HAND_ZONE;
    public static final int IN_LIBRARY_ZONE;
    public static final int BLOCKS_ATTACKER;
    public static final int CANNOT_ATTACK;
    public static final int CANNOT_BLOCK;
    public static final int CANNOT_BE_BLOCKED;
    public static final int ATTACKS_TARGET;
    public static final int CAST_TARGET;
    public static final int MANA;
    public static final int MANA_POOL;
    public static final int MANA_GROWTH;
    public static final int DRAW_CARDS;
    public static final int DRAWS_PER_TURN;
    public static final int DRAWS_ON_ATTACK;
    public static final int DRAWS_ON_BLOCK;
    public static final int DRAWS_ON_ATTACKED;
    public static final int FATIGUE;
    public static final int TIRED;
    public static final int CARD_TEMPLATE;
    public static final int MINION_TEMPLATE;
    public static final int SUMMON_ON_SURVIVAL;
    public static final int SUMMON_ON_DEATH;

    static {
        HAS_LOST = createMeta("HAS_LOST");
        HAS_WON = createMeta("HAS_WON");
        PLAYER_INDEX = createMeta("PLAYER_INDEX");
        ACTIVE_PLAYER_PHASE = createMeta("ACTIVE_PLAYER_PHASE");
        END_BLOCK_PHASE = createMeta("END_BLOCK_PHASE");
        END_ATTACK_PHASE = createMeta("END_ATTACK_PHASE");
        HEALTH = createMeta("HEALTH");
        ATTACK = createMeta("ATTACK");
        DAMAGE = createMeta("DAMAGE");
        DIE = createMeta("DIE");
        OWNED_BY = createMeta("OWNED_BY");
        IN_BATTLE_ZONE = createMeta("IN_BATTLE_ZONE");
        IN_HAND_ZONE = createMeta("IN_HAND_ZONE");
        IN_LIBRARY_ZONE = createMeta("IN_LIBRARY_ZONE");
        CANNOT_ATTACK = createMeta("CANNOT_ATTACK");
        CANNOT_BLOCK = createMeta("CANNOT_BLOCK");
        CANNOT_BE_BLOCKED = createMeta("CANNOT_BE_BLOCKED");
        BLOCKS_ATTACKER = createMeta("BLOCKS_ATTACKER");
        ATTACKS_TARGET = createMeta("ATTACKS_PLAYER");
        CAST_TARGET = createMeta("CAST_TARGET");
        MANA = createMeta("MANA");
        MANA_POOL = createMeta("MANA_POOL");
        MANA_GROWTH = createMeta("MANA_GROWTH");
        DRAW_CARDS = createMeta("DRAW_CARDS");
        DRAWS_PER_TURN = createMeta("DRAWS_PER_TURN");
        DRAWS_ON_ATTACK = createMeta("DRAWS_ON_ATTACK");
        DRAWS_ON_BLOCK = createMeta("DRAWS_ON_BLOCK");
        DRAWS_ON_ATTACKED = createMeta("DRAWS_ON_ATTACKED");
        FATIGUE = createMeta("FATIGUE");
        TIRED = createMeta("TIRED");
        CARD_TEMPLATE = createMeta("CARD_TEMPLATE");
        MINION_TEMPLATE = createMeta("MINION_TEMPLATE");
        SUMMON_ON_SURVIVAL = createMeta("SUMMON_ON_SURVIVAL");
        SUMMON_ON_DEATH = createMeta("SUMMON_ON_DEATH");
    }

    public static ComponentMeta getComponent(int id) {
        return COMPONENTS.get(id);
    }

    public static int count() {
        return COMPONENTS.size();
    }

    public static List<ComponentMeta> getComponents() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    private static int createMeta(String name) {
        ComponentMeta meta = new ComponentMeta(COMPONENTS.size(), name);
        COMPONENTS.add(meta);
        return meta.id;
    }
}
