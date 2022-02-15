package com.etherblood.ethercards.templates.api;

import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public int getAliasId(String alias) {
        return parser.resolveCardAlias(alias);
    }

    private JsonObject load(String alias) {
        return assetLoader.apply(alias).getAsJsonObject();
    }

    public static JsonElement loadFile(String file) {
        try (Reader reader = Files.newBufferedReader(Paths.get(file))) {
            return new Gson().fromJson(reader, JsonElement.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
