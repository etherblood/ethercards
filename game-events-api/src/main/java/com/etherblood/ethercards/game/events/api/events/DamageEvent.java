package com.etherblood.ethercards.game.events.api.events;

public class DamageEvent {

    public final int minion, damage;

    public DamageEvent(int minion, int damage) {
        this.minion = minion;
        this.damage = damage;
    }
}
