package com.etherblood.a.templates.api;

import com.etherblood.a.templates.api.deserializers.ComponentsDeserializer;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.entities.Components;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.TargetSelection;
import com.etherblood.a.templates.api.deserializers.RawZoneStateDeserializer;
import com.etherblood.a.templates.api.deserializers.TemplateObjectDeserializer;
import com.etherblood.a.templates.api.model.RawCardTemplate;
import com.etherblood.a.templates.api.model.RawZoneState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplatesParser {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatesParser.class);

    private final Map<Integer, DisplayCardTemplate> cards = new HashMap<>();
    private final Map<String, Integer> cardAliases = new HashMap<>();
    private final Map<String, Integer> componentAliases = new HashMap<>();
    private final Gson aliasGson;

    public TemplatesParser(Components components, TemplateClassAliasMap templateClassAliasMap) {
        for (ComponentMeta component : components.getMetas()) {
            componentAliases.put(component.name, component.id);
        }
        aliasGson = new GsonBuilder()
                .registerTypeAdapter(Effect.class, new TemplateObjectDeserializer<>(templateClassAliasMap.getEffects(), x -> registerIfAbsent(cardAliases, x), componentAliases::get))
                .registerTypeAdapter(StatModifier.class, new TemplateObjectDeserializer<>(templateClassAliasMap.getStatModifiers(), x -> registerIfAbsent(cardAliases, x), componentAliases::get))
                .registerTypeAdapter(TargetSelection.class, new TemplateObjectDeserializer<>(templateClassAliasMap.getTargetSelections(), x -> registerIfAbsent(cardAliases, x), componentAliases::get))
                .registerTypeAdapter(TargetPredicate.class, new TemplateObjectDeserializer<>(templateClassAliasMap.getTargetPredicates(), x -> registerIfAbsent(cardAliases, x), componentAliases::get))
                .registerTypeAdapter(IntMap.class, new ComponentsDeserializer(x -> registerIfAbsent(cardAliases, x), componentAliases::get))
                .registerTypeAdapter(RawZoneState.class, new RawZoneStateDeserializer(componentAliases::get))
                .create();
    }

    public GameTemplates buildGameTemplates() {
        if (unresolvedCards().isEmpty()) {
            return new GameTemplates(cards);
        }
        throw new AssertionError();
    }

    public LibraryTemplate parseLibrary(RawLibraryTemplate raw) {
        LibraryTemplate library = new LibraryTemplate();
        library.hero = registerIfAbsent(cardAliases, raw.hero);
        IntList libraryCards = new IntList();
        Map<String, Integer> cardsObject = raw.cards;
        for (Map.Entry<String, Integer> entry : cardsObject.entrySet()) {
            int count = entry.getValue();
            int cardId = registerIfAbsent(cardAliases, entry.getKey());
            for (int i = 0; i < count; i++) {
                libraryCards.add(cardId);
            }
        }
        library.cards = libraryCards.toArray();
        return library;
    }

    public DisplayCardTemplate parseCard(JsonObject cardJson) {
        try {
            RawCardTemplate rawCard = aliasGson.fromJson(cardJson, RawCardTemplate.class);
            int id = registerIfAbsent(cardAliases, rawCard.alias);

            DisplayCardTemplate card = new DisplayCardTemplate(
                    id,
                    !rawCard.battle.components.isEmpty(),
                    rawCard.alias,
                    rawCard.display.name,
                    rawCard.display.flavourText,
                    rawCard.display.description,
                    rawCard.display.imagePath,
                    rawCard.display.colors,
                    rawCard.tribes,
                    rawCard.hand.toZoneState(),
                    rawCard.battle.toZoneState(),
                    rawCard.graveyard.toZoneState(),
                    rawCard.library.toZoneState());
            DisplayCardTemplate previous = cards.put(id, card);
            if (previous != null) {
                throw new IllegalStateException("Multiple cards registered to same alias: " + rawCard.alias);
            }
            return card;
        } catch (Exception ex) {
            LOG.error("Failed to parse card {}.", cardJson.get("alias"));
            throw ex;
        }
    }

    public Set<String> unresolvedCards() {
        return cardAliases.entrySet().stream()
                .filter(e -> !cards.containsKey(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public int resolveCardAlias(String alias) {
        return Objects.requireNonNull(cardAliases.get(alias), alias);
    }

    public int registerCardAlias(String alias) {
        return registerIfAbsent(cardAliases, alias);
    }

    private int registerIfAbsent(Map<String, Integer> aliases, String alias) {
        int nextId = aliases.size();
        Integer previousId = aliases.putIfAbsent(alias, nextId);
        if (previousId != null) {
            return previousId;
        }
        return nextId;
    }

}
