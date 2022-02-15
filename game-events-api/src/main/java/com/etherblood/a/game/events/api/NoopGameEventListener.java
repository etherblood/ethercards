package com.etherblood.ethercards.game.events.api;

public class NoopGameEventListener implements GameEventListener {

    @Override
    public void fire(Object event) {
        //do nothing
    }

}
