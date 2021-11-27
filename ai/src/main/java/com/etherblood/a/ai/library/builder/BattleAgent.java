package com.etherblood.a.ai.library.builder;

import com.etherblood.a.templates.api.setup.RawLibraryTemplate;

public class BattleAgent {
    public final RawLibraryTemplate library;
    public int wins;
    public int losses;
    public int draws;

    public BattleAgent(RawLibraryTemplate library) {
        this.library = library;
    }
}
