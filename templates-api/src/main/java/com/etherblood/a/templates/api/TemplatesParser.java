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
import com.etherblood.a.templates.api.deserializers.TemplateObjectDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
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
        String alias = cardJson.get("alias").getAsString();
        try {
            DisplayCardTemplateBuilder builder = new DisplayCardTemplateBuilder();
            builder.setAlias(alias);
            JsonObject display = cardJson.getAsJsonObject("display");
            if (display != null) {
                JsonElement colors = display.get("colors");
                if (colors != null) {
                    builder.setColors(Arrays.asList(aliasGson.fromJson(colors, CardColor[].class)));
                }
                JsonElement name = display.get("name");
                if (name != null) {
                    builder.setName(name.getAsString());
                }
                JsonElement description = display.get("description");
                if (description != null) {
                    builder.setDescription(description.getAsString());
                }
                JsonElement flavourText = display.get("flavourText");
                if (flavourText != null) {
                    builder.setFlavourText(flavourText.getAsString());
                }
                JsonElement imagePath = display.get("imagePath");
                if (imagePath != null && !imagePath.isJsonNull()) {
                    builder.setImagePath(imagePath.getAsString());
                }
            } else {
                builder.setColors(Arrays.asList(CardColor.values()));
                builder.setName("MissingNo #" + alias);
                builder.setDescription("404");
                builder.setFlavourText("Nothing here.");
                builder.setImagePath(null);
            }

            JsonElement targetJson = cardJson.get("target");
            if (targetJson != null) {
                builder.setCastTarget(aliasGson.fromJson(targetJson, TargetSelection.class));
            } else {
                builder.setCastTarget(new Untargeted());
            }

            JsonArray castEffects = cardJson.getAsJsonArray("cast");
            if (castEffects != null) {
                for (JsonElement castEffect : castEffects) {
                    builder.castEffect(aliasGson.fromJson(castEffect, Effect.class));
                }
            }

            JsonElement manaCost = cardJson.get("manaCost");
            if (manaCost != null) {
                builder.setManaCost(manaCost.getAsInt());
            }
            JsonArray tribes = cardJson.getAsJsonArray("tribes");
            if (tribes != null) {
                for (JsonElement tribe : tribes) {
                    builder.addTribe(aliasGson.fromJson(tribe, Tribe.class));
                }
            }
            
            JsonObject inBattle = cardJson.getAsJsonObject("battle");
            if(inBattle != null) {
                for (Map.Entry<String, JsonElement> entry : inBattle.entrySet()) {
                    JsonArray effects = entry.getValue().getAsJsonArray();
                    for (JsonElement effectJson : effects) {
                        builder.inBattle(componentAliases.get(entry.getKey()), aliasGson.fromJson(effectJson, Effect.class));
                    }
                }
            }
            JsonObject inHand = cardJson.getAsJsonObject("hand");
            if(inHand != null) {
                for (Map.Entry<String, JsonElement> entry : inHand.entrySet()) {
                    JsonArray effects = entry.getValue().getAsJsonArray();
                    for (JsonElement effectJson : effects) {
                        builder.inHand(componentAliases.get(entry.getKey()), aliasGson.fromJson(effectJson, Effect.class));
                    }
                }
            }
            JsonObject inLibrary = cardJson.getAsJsonObject("library");
            if(inLibrary != null) {
                for (Map.Entry<String, JsonElement> entry : inLibrary.entrySet()) {
                    JsonArray effects = entry.getValue().getAsJsonArray();
                    for (JsonElement effectJson : effects) {
                        builder.inLibrary(componentAliases.get(entry.getKey()), aliasGson.fromJson(effectJson, Effect.class));
                    }
                }
            }
            JsonObject inGraveyard = cardJson.getAsJsonObject("graveyard");
            if(inGraveyard != null) {
                for (Map.Entry<String, JsonElement> entry : inGraveyard.entrySet()) {
                    JsonArray effects = entry.getValue().getAsJsonArray();
                    for (JsonElement effectJson : effects) {
                        builder.inGraveyard(componentAliases.get(entry.getKey()), aliasGson.fromJson(effectJson, Effect.class));
                    }
                }
            }
            
            JsonObject componentModifiers = cardJson.getAsJsonObject("componentModifiers");
            if (componentModifiers != null) {
                for (Map.Entry<String, JsonElement> entry : componentModifiers.entrySet()) {
                    Integer component = componentAliases.get(entry.getKey());
                    for (StatModifier modifier : aliasGson.fromJson(entry.getValue(), StatModifier[].class)) {
                        builder.modifyComponent(component, modifier);
                    }
                }
            }

            JsonObject cardComponents = cardJson.getAsJsonObject("components");
            if (cardComponents != null) {
                for (Map.Entry<String, JsonElement> entry : cardComponents.entrySet()) {
                    Integer component = componentAliases.get(entry.getKey());
                    if (component == null) {
                        throw new NullPointerException("No component found for alias " + alias + ".");
                    }
                    JsonElement value = entry.getValue();
                    if (value.isJsonNull()) {
                        builder.remove(component);
                        continue;
                    }
                    if (value.isJsonPrimitive()) {
                        JsonPrimitive primitive = value.getAsJsonPrimitive();
                        if (primitive.isNumber()) {
                            builder.set(component, primitive.getAsInt());
                            continue;
                        }
                        if (primitive.isBoolean()) {
                            builder.set(component, primitive.getAsBoolean() ? 1 : 0);
                            continue;
                        }
                    }
                    if (value.isJsonObject()) {
                        JsonObject obj = value.getAsJsonObject();
                        if (obj.has("minion")) {
                            int minion = registerIfAbsent(cardAliases, obj.getAsJsonPrimitive("minion").getAsString());
                            builder.set(component, minion);
                            continue;
                        }
                        if (obj.has("card")) {
                            int card = registerIfAbsent(cardAliases, obj.getAsJsonPrimitive("card").getAsString());
                            builder.set(component, card);
                            continue;
                        }
                    }
                    throw new UnsupportedOperationException(value.toString());
                }
            }
            return register(builder);
        } catch (Exception ex) {
            LOG.error("Failed to parse card {}.", alias);
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
