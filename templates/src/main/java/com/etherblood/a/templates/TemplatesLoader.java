package com.etherblood.a.templates;

import com.google.gson.JsonObject;
import java.util.Set;
import java.util.function.Function;

public class TemplatesLoader {

    private final TemplatesParser parser;
    private final Function<String, JsonObject> assetLoader;

    public TemplatesLoader(Function<String, JsonObject> assetLoader, TemplatesParser parser) {
        this.assetLoader = assetLoader;
        this.parser = parser;
    }

    public LibraryTemplate loadLibrary(String alias) {
        LibraryTemplate library = parser.getLibrary(alias);
        if (library == null) {
            library = parser.parseLibrary(load(alias));
        }
        Set<String> cards, minions;
        while (!(cards = parser.unresolvedCards()).isEmpty() | !(minions = parser.unresolvedMinions()).isEmpty()) {
            for (String minion : minions) {
                parser.parseMinion(load(minion));
            }
            for (String card : cards) {
                parser.parseCard(load(card));
            }
        }
        return library;
    }

    public DisplayCardTemplate getCard(int id) {
        return parser.getCard(id);
    }

    public DisplayMinionTemplate getMinion(int id) {
        return parser.getMinion(id);
    }

    private JsonObject load(String alias) {
        return assetLoader.apply(alias);
    }
}
