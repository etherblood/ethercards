package com.etherblood.ethercards.templates.api.setup;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RawLibraryTemplate)) {
            return false;
        }

        RawLibraryTemplate that = (RawLibraryTemplate) o;
        return hero.equals(that.hero) && cards.equals(that.cards);
    }

    @Override
    public int hashCode() {
        int result = hero.hashCode();
        result = 31 * result + cards.hashCode();
        return result;
    }
}
