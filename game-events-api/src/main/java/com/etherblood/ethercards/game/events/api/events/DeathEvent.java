package com.etherblood.ethercards.game.events.api.events;

public class DeathEvent {

    public final int minion;

    public DeathEvent(int minion) {
        this.minion = minion;
    }
}
