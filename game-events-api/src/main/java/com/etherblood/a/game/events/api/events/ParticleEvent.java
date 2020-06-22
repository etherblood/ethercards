package com.etherblood.a.game.events.api.events;

public class ParticleEvent {

    public final String alias;
    public final int source, target;

    public ParticleEvent(String alias, int source, int target) {
        this.alias = alias;
        this.source = source;
        this.target = target;
    }

}
