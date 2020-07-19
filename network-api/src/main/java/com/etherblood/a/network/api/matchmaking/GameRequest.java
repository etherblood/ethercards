package com.etherblood.a.network.api.matchmaking;

import com.etherblood.a.templates.api.setup.RawLibraryTemplate;

public class GameRequest {

    public String jwt;
    public RawLibraryTemplate library;
    public int strength;
    public int[] teamHumanCounts;
    public int teamSize;

    public GameRequest(String jwt, RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        this.jwt = jwt;
        this.library = library;
        this.strength = strength;
        this.teamHumanCounts = teamHumanCounts;
        this.teamSize = teamSize;
    }


    GameRequest() {
    }

}
