package com.etherblood.a.entities.collections;

interface IntKeyValueIterator {

    boolean next();

    int key();

    int value();
}
