package com.etherblood.ethercards.templates.api.setup;

import java.util.LinkedHashMap;
import java.util.Map;

public record RawLibraryTemplate(
        String hero,
        Map<String, Integer> cards
) {
    
    public RawLibraryTemplate(RawLibraryTemplate other) {
        this(other.hero, new LinkedHashMap<>(other.cards));
    }

}
