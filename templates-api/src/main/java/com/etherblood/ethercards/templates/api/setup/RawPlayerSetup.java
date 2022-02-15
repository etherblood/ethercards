package com.etherblood.ethercards.templates.api.setup;

import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.rules.setup.PlayerSetup;
import java.util.Map;
import java.util.function.ToIntFunction;

public class RawPlayerSetup {

    public long id;
    public int teamIndex;
    public String name;
    public RawLibraryTemplate library;

    public RawPlayerSetup(RawPlayerSetup other) {
        this.id = other.id;
        this.teamIndex = other.teamIndex;
        this.name = other.name;
        this.library = new RawLibraryTemplate(other.library);
    }

    public RawPlayerSetup() {
    }

    PlayerSetup toPlayerSetup(ToIntFunction<String> cardAliasResolver) {
        PlayerSetup setup = new PlayerSetup();
        setup.heroId = cardAliasResolver.applyAsInt(library.hero);
        setup.libraryCardCounts = new IntMap();
        for (Map.Entry<String, Integer> entry : library.cards.entrySet()) {
            setup.libraryCardCounts.set(cardAliasResolver.applyAsInt(entry.getKey()), entry.getValue());
        }
        setup.teamIndex = teamIndex;
        return setup;
    }
}
