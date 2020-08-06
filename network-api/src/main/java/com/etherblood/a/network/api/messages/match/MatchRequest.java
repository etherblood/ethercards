package com.etherblood.a.network.api.messages.match;

import com.etherblood.a.templates.api.setup.RawLibraryTemplate;

public class MatchRequest {

    public RawLibraryTemplate library;
    public int strength;
    public int[] teamHumanCounts;
    public int teamSize;

    public MatchRequest(RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        this.library = library;
        this.strength = strength;
        this.teamHumanCounts = teamHumanCounts;
        this.teamSize = teamSize;
    }

    MatchRequest() {
    }

}
