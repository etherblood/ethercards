package com.etherblood.ethercards.entities.collections;

interface IntKeyValueIterator {

    boolean next();

    int key();

    int value();
}
