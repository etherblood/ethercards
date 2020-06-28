package com.etherblood.a.rules.moves;

public class Start implements Move {

    @Override
    public int hashCode() {
        return 13;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Start;
    }

    @Override
    public String toString() {
        return "Start{" + '}';
    }

    @Override
    public Integer getPlayer() {
        return null;
    }

}
