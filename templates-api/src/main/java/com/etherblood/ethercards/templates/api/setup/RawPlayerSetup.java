package com.etherblood.ethercards.templates.api.setup;

import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.rules.setup.PlayerSetup;
import java.util.Map;
import java.util.function.ToIntFunction;

public record RawPlayerSetup(
        long id,
        String name,
        int teamIndex,
        RawLibraryTemplate library
) {

    public RawPlayerSetup(RawPlayerSetup other) {
        this(other.id, other.name, other.teamIndex, new RawLibraryTemplate(other.library));
    }

    PlayerSetup toPlayerSetup(ToIntFunction<String> cardAliasResolver) {
        PlayerSetup setup = new PlayerSetup();
        setup.heroId = cardAliasResolver.applyAsInt(library.hero());
        setup.libraryCardCounts = new IntMap();
        for (Map.Entry<String, Integer> entry : library.cards().entrySet()) {
            setup.libraryCardCounts.set(cardAliasResolver.applyAsInt(entry.getKey()), entry.getValue());
        }
        setup.teamIndex = teamIndex;
        return setup;
    }
}
