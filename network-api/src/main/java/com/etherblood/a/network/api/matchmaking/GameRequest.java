package com.etherblood.a.network.api.matchmaking;

import com.etherblood.a.templates.api.RawLibraryTemplate;

public class GameRequest {

    public String jwt;
    public RawLibraryTemplate library;
    public OpponentType opponent;
    public int strength;

    GameRequest() {
    }

    public GameRequest(String jwt, RawLibraryTemplate library, OpponentType opponent, int strength) {
        this.jwt = jwt;
        this.library = library;
        this.opponent = opponent;
        this.strength = strength;
    }
}
