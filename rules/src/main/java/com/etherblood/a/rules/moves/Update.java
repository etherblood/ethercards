package com.etherblood.a.rules.moves;

public class Update implements Move {

    @Override
    public int hashCode() {
        return 13;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Update;
    }

    @Override
    public String toString() {
        return "Update{" + '}';
    }

    @Override
    public Integer getPlayer() {
        return null;
    }

}
