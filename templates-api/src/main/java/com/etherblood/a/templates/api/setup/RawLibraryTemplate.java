package com.etherblood.a.templates.api.setup;

import java.util.LinkedHashMap;
import java.util.Map;

public class RawLibraryTemplate {

    public String hero;
    public Map<String, Integer> cards;

    public RawLibraryTemplate(RawLibraryTemplate other) {
        this.hero = other.hero;
        this.cards = new LinkedHashMap<>(other.cards);
    }

    public RawLibraryTemplate() {
    }
}
