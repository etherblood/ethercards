package com.etherblood.a.templates;

import com.etherblood.a.rules.GameTemplates;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Set;
import java.util.function.Function;

public class TemplatesLoader {

    private final TemplatesParser parser;
    private final Function<String, JsonElement> assetLoader;

    public TemplatesLoader(Function<String, JsonElement> assetLoader, TemplatesParser parser) {
        this.assetLoader = assetLoader;
        this.parser = parser;
    }

    public GameTemplates buildGameTemplates() {
        resolveReferences();
        return parser.buildGameTemplates();
    }
    
    public LibraryTemplate parseLibrary(RawLibraryTemplate raw) {
        return parser.parseLibrary(raw);
    }

    private void resolveReferences() {
        Set<String> cards;
        while (!(cards = parser.unresolvedCards()).isEmpty()) {
            for (String card : cards) {
                parser.parseCard(load(card));
            }
        }
    }
    
    public int registerCardAlias(String alias) {
        return parser.registerCardAlias(alias);
    }

    private JsonObject load(String alias) {
        return assetLoader.apply(alias).getAsJsonObject();
    }
}
