package com.etherblood.a.game.events.api;

/**
 *
 * @author Philipp
 */
public interface GameEventListener {

    void fire(Object event);
    
    void nextIteration();
}
