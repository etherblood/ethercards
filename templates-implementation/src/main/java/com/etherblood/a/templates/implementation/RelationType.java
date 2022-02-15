package com.etherblood.ethercards.templates.implementation;

public enum RelationType {
    EQUAL,
    NOT_EQUAL,
    LESS_THAN,
    GREATER_THAN,
    LESS_OR_EQUAL,
    GREATER_OR_EQUAL;

    public boolean applyTo(int a, int b) {
        switch (this) {
            case EQUAL:
                return a == b;
            case NOT_EQUAL:
                return a != b;
            case LESS_THAN:
                return a < b;
            case GREATER_THAN:
                return a > b;
            case LESS_OR_EQUAL:
                return a <= b;
            case GREATER_OR_EQUAL:
                return a >= b;
            default:
                throw new AssertionError(this.name());
        }
    }
}
