package com.etherblood.ethercards.game.events.api.events;

public class BattleEvent {

    public final int minionA, minionB;

    public BattleEvent(int minionA, int minionB) {
        this.minionA = minionA;
        this.minionB = minionB;
    }
}
