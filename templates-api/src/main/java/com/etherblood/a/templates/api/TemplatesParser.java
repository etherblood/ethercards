package com.etherblood.a.templates.api;

import com.etherblood.a.templates.api.deserializers.ComponentsDeserializer;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.entities.Components;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.TargetSelection;
import com.etherblood.a.templates.api.deserializers.ZoneStateDeserializer;
import com.etherblood.a.templates.api.deserializers.TemplateObjectDeserializer;
import com.etherblood.a.templates.api.model.RawCardDisplay;
import com.etherblood.a.templates.api.model.RawCardTemplate;
import com.etherblood.a.rules.templates.ZoneState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
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
                .registerTypeAdapter(ZoneState.class, new ZoneStateDeserializer(componentAliases::get))
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
        RawCardTemplate rawCard = aliasGson.fromJson(cardJson, RawCardTemplate.class);

        try {
            DisplayCardTemplateBuilder builder = new DisplayCardTemplateBuilder();
            builder.setAlias(rawCard.alias);
            RawCardDisplay display = rawCard.display;
            if (display != null) {
                builder.setColors(display.colors);
                builder.setDescription(display.description);
                builder.setFlavourText(display.flavourText);
                builder.setImagePath(display.imagePath);
                builder.setName(display.name);
            }
            builder.setManaCost(rawCard.hand.cast.getManaCost());
            if (rawCard.hand.cast.getTarget() != null) {
                builder.setCastTarget(rawCard.hand.cast.getTarget());
            } else {
                builder.setCastTarget(new Untargeted());
            }
            for (Effect effect : rawCard.hand.cast.getEffects()) {
                builder.castEffect(effect);
            }

            for (Tribe tribe : rawCard.tribes) {
                builder.addTribe(tribe);
            }
            for (Map.Entry<Integer, List<Effect>> entry : rawCard.battle.passive.entrySet()) {
                for (Effect effect : entry.getValue()) {
                    builder.inBattle(entry.getKey(), effect);
                }
            }
            for (Map.Entry<Integer, List<Effect>> entry : rawCard.hand.passive.entrySet()) {
                for (Effect effect : entry.getValue()) {
                    builder.inHand(entry.getKey(), effect);
                }
            }
            for (Map.Entry<Integer, List<Effect>> entry : rawCard.graveyard.passive.entrySet()) {
                for (Effect effect : entry.getValue()) {
                    builder.inGraveyard(entry.getKey(), effect);
                }
            }
            for (Map.Entry<Integer, List<Effect>> entry : rawCard.library.passive.entrySet()) {
                for (Effect effect : entry.getValue()) {
                    builder.inLibrary(entry.getKey(), effect);
                }
            }

            for (Map.Entry<Integer, List<StatModifier>> entry : rawCard.battle.componentModifiers.entrySet()) {
                for (StatModifier statModifier : entry.getValue()) {
                    builder.modifyComponent(entry.getKey(), statModifier);
                }
            }

            builder.setBattleAbility(rawCard.battle.activated);
            if (rawCard.battle.components != null) {
                for (int component : rawCard.battle.components) {
                    builder.set(component, rawCard.battle.components.get(component));
                }
            }
            return register(builder);
        } catch (Exception ex) {
            LOG.error("Failed to parse card {}.", rawCard.alias);
            throw ex;
        }
    }

    public DisplayCardTemplate register(DisplayCardTemplateBuilder builder) {
        int id = registerIfAbsent(cardAliases, builder.getAlias());
        DisplayCardTemplate card = builder.build(id);
        DisplayCardTemplate previous = cards.put(id, card);
        if (previous != null) {
            throw new IllegalStateException("Multiple cards registered to same alias: " + builder.getAlias());
        }
        return card;
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
