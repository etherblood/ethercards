package com.etherblood.a.templates;

public class DisplayStats {

    private final int attack, health;

    public DisplayStats(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    public int getAttack() {
        return attack;
    }

    public int getHealth() {
        return health;
    }
}
