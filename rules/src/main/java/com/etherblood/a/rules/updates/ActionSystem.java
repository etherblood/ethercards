package com.etherblood.a.rules.updates;

/**
 *
 * @author Philipp
 */
public interface ActionSystem {

    boolean isActive();

    void modify();

    void apply();

    void triggerAndClean();
}
