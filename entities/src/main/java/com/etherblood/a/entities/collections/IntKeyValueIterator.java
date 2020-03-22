package com.etherblood.a.entities.collections;

/**
 *
 * @author Philipp
 */
interface IntKeyValueIterator {

    boolean next();

    int key();

    int value();
}
