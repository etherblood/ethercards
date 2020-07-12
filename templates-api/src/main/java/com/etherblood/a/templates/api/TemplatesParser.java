package com.etherblood.a.templates.api;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.entities.Components;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardCastBuilder;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.targeting.TargetFilters;
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

    public TemplatesParser(Components components, Map<String, Class<? extends Effect>> effectClasses, Map<String, Class<? extends StatModifier>> modifierClasses) {
        for (ComponentMeta component : components.getMetas()) {
            componentAliases.put(component.name, component.id);
        }
        aliasGson = new GsonBuilder()
                .registerTypeAdapter(Effect.class, new EffectDeserializer(effectClasses, x -> registerIfAbsent(cardAliases, x), componentAliases::get))
                .registerTypeAdapter(StatModifier.class, new StatModifierDeserializer(modifierClasses))
                .registerTypeAdapter(IntMap.class, new ComponentsDeserializer(componentAliases::get))
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

            JsonArray casts = cardJson.getAsJsonArray("casts");
            if (casts != null) {
                for (JsonElement castElement : casts) {
                    JsonObject castJson = castElement.getAsJsonObject();
                    CardCastBuilder cast = builder.newCast();
                    JsonPrimitive manaCost = castJson.getAsJsonPrimitive("manaCost");
                    if (manaCost != null) {
                        builder.setManaCost(manaCost.getAsInt());
                    }
                    if (castJson.has("targets")) {
                        cast.setTargets(aliasGson.fromJson(castJson.getAsJsonArray("targets"), TargetFilters[].class));
                    }
                    if (castJson.has("targetOptional")) {
                        cast.setTargetOptional(castJson.get("targetOptional").getAsBoolean());
                    }
                    for (JsonElement jsonElement : castJson.getAsJsonArray("effects")) {
                        JsonObject effectJson = jsonElement.getAsJsonObject();
                        cast.addEffect(aliasGson.fromJson(effectJson, Effect.class));
                    }
                    if (castJson.has("attackCast")) {
                        cast.setAttackCast(castJson.get("attackCast").getAsBoolean());
                    }
                    if (castJson.has("blockCast")) {
                        cast.setBlockCast(castJson.get("blockCast").getAsBoolean());
                    }
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
            JsonArray onDeath = cardJson.getAsJsonArray("onDeath");
            if (onDeath != null) {
                for (JsonElement jsonElement : onDeath) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.onDeath(aliasGson.fromJson(effectJson, Effect.class));
                }
            }
            JsonArray onSurvive = cardJson.getAsJsonArray("onSurvive");
            if (onSurvive != null) {
                for (JsonElement jsonElement : onSurvive) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.onSurvive(aliasGson.fromJson(effectJson, Effect.class));
                }
            }
            JsonArray onUpkeep = cardJson.getAsJsonArray("onUpkeep");
            if (onUpkeep != null) {
                for (JsonElement jsonElement : onUpkeep) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.onUpkeep(aliasGson.fromJson(effectJson, Effect.class));
                }
            }
            JsonArray afterBattle = cardJson.getAsJsonArray("afterBattle");
            if (afterBattle != null) {
                for (JsonElement jsonElement : afterBattle) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.afterBattle(aliasGson.fromJson(effectJson, Effect.class));
                }
            }
            JsonArray onCast = cardJson.getAsJsonArray("onCast");
            if (onCast != null) {
                for (JsonElement jsonElement : onCast) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.onCast(aliasGson.fromJson(effectJson, Effect.class));
                }
            }
            JsonArray onSummon = cardJson.getAsJsonArray("onSummon");
            if (onSummon != null) {
                for (JsonElement jsonElement : onSummon) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.onSummon(aliasGson.fromJson(effectJson, Effect.class));
                }
            }
            JsonArray onDraw = cardJson.getAsJsonArray("onDraw");
            if (onDraw != null) {
                for (JsonElement jsonElement : onDraw) {
                    JsonObject effectJson = jsonElement.getAsJsonObject();
                    builder.onDraw(aliasGson.fromJson(effectJson, Effect.class));
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
