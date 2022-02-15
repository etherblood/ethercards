package com.etherblood.ethercards.ai.library.builder;

import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;

public class BattleAgent {
    public final RawLibraryTemplate library;
    public int wins;
    public int losses;
    public int draws;

    public BattleAgent(RawLibraryTemplate library) {
        this.library = library;
    }
}
