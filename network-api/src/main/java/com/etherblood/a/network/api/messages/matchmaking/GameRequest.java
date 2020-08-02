package com.etherblood.a.network.api.messages.matchmaking;

import com.etherblood.a.templates.api.setup.RawLibraryTemplate;

public class GameRequest {

    public RawLibraryTemplate library;
    public int strength;
    public int[] teamHumanCounts;
    public int teamSize;

    public GameRequest(RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        this.library = library;
        this.strength = strength;
        this.teamHumanCounts = teamHumanCounts;
        this.teamSize = teamSize;
    }

    GameRequest() {
    }

}
