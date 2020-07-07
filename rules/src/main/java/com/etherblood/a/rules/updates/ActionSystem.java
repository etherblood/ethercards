package com.etherblood.a.rules.updates;

/**
 *
 * @author Philipp
 */
public interface ActionSystem {

    boolean isActive();

    void before();

    void run();

    void after();
}
