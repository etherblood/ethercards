package com.etherblood.ethercards.gui.matchmaking;

public class MatchOpponents {

    public final int[] teamHumanCounts;
    public final int teamSize;
    public final int strength;

    public MatchOpponents(int[] teamHumanCounts, int teamSize, int strength) {
        this.teamHumanCounts = teamHumanCounts;
        this.teamSize = teamSize;
        this.strength = strength;
    }
}
