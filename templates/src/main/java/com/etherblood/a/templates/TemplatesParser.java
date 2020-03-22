package com.etherblood.a.templates;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.ComponentMeta;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.templates.CardCastBuilder;
import com.etherblood.a.rules.templates.casteffects.CastEffect;
import com.etherblood.a.rules.templates.casteffects.SingleTargetDamageEffect;
import com.etherblood.a.rules.templates.casteffects.SummonEffect;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplatesParser {

    private final Map<String, LibraryTemplate> libraries = new HashMap<>();
    private final Map<Integer, DisplayCardTemplate> cards = new HashMap<>();
    private final Map<Integer, DisplayMinionTemplate> minions = new HashMap<>();
    private final Map<String, Integer> cardAliases = new HashMap<>();
    private final Map<String, Integer> minionAliases = new HashMap<>();
    private final Map<String, Integer> componentAliases = new HashMap<>();
    private final Gson aliasGson;

    public TemplatesParser() {
        Map<String, Class<? extends CastEffect>> classes = new HashMap<>();
        classes.put("summon", SummonEffect.class);
        classes.put("singleTargetDamage", SingleTargetDamageEffect.class);
        aliasGson = new GsonBuilder().registerTypeAdapter(CastEffect.class, new CastEffectDeserializer(classes, x -> registerIfAbsent(minionAliases, x), x -> registerIfAbsent(cardAliases, x))).create();

        for (ComponentMeta component : Components.getComponents()) {
            componentAliases.put(component.name, component.id);
        }
    }

    public DisplayCardTemplate getCard(int id) {
        DisplayCardTemplate card = cards.get(id);
        if (card == null) {
            for (Map.Entry<String, Integer> entry : cardAliases.entrySet()) {
                if (entry.getValue() == id) {
                    throw new NullPointerException("No card template registered for alias '" + entry.getKey() + "'.");
                }
            }
            throw new NullPointerException("No card template or alias found for id '" + id + "'.");
        }
        return card;
    }

    public DisplayMinionTemplate getMinion(int id) {
        DisplayMinionTemplate minion = minions.get(id);
        if (minion == null) {
            for (Map.Entry<String, Integer> entry : minionAliases.entrySet()) {
                if (entry.getValue() == id) {
                    throw new NullPointerException("No minion template registered for alias '" + entry.getKey() + "'.");
                }
            }
            throw new NullPointerException("No minion template or alias found for id '" + id + "'.");
        }
        return minion;
    }

    public LibraryTemplate getLibrary(String alias) {
        return libraries.get(alias);
    }

    public LibraryTemplate parseLibrary(JsonObject json) {
        LibraryTemplate library = new LibraryTemplate();
        library.hero = registerIfAbsent(minionAliases, json.get("hero").getAsString());
        IntList libraryCards = new IntList();
        JsonObject cardsObject = json.getAsJsonObject("cards");
        for (Map.Entry<String, JsonElement> entry : cardsObject.entrySet()) {
            int count = entry.getValue().getAsInt();
            int cardId = registerIfAbsent(cardAliases, entry.getKey());
            for (int i = 0; i < count; i++) {
                libraryCards.add(cardId);
            }
        }
        library.cards = libraryCards.toArray();
        String alias = json.getAsJsonPrimitive("alias").getAsString();
        LibraryTemplate previous = libraries.put(alias, library);
        if (previous != null) {
            throw new IllegalStateException("Multiple libraries registered to same alias: " + alias);
        }
        return library;
    }

    public DisplayCardTemplate parseCard(JsonObject cardJson) {
        String alias = cardJson.get("alias").getAsString();
        int id = registerIfAbsent(cardAliases, alias);
        DisplayCardTemplateBuilder builder = new DisplayCardTemplateBuilder(id);
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
            builder.setName("MissingNo #" + id);
            builder.setDescription("404");
            builder.setFlavourText("Nothing here.");
            builder.setImagePath(null);
        }

        JsonObject castJson = cardJson.getAsJsonObject("attackCast");
        if (castJson != null && !castJson.isJsonNull()) {
            CardCastBuilder cast = builder.getAttackPhaseCast();
            cast.setManaCost(castJson.getAsJsonPrimitive("manaCost").getAsInt());
            if (castJson.has("targeted")) {
                cast.setTargeted(castJson.getAsJsonPrimitive("targeted").getAsBoolean());
            }
            for (JsonElement jsonElement : castJson.getAsJsonArray("effects")) {
                JsonObject effectJson = jsonElement.getAsJsonObject();
                cast.addEffect(aliasGson.fromJson(effectJson, CastEffect.class));
            }
        }

        castJson = cardJson.getAsJsonObject("blockCast");
        if (castJson != null && !castJson.isJsonNull()) {
            CardCastBuilder cast = builder.getBlockPhaseCast();
            cast.setManaCost(castJson.getAsJsonPrimitive("manaCost").getAsInt());
            if (castJson.has("targeted")) {
                cast.setTargeted(castJson.getAsJsonPrimitive("targeted").getAsBoolean());
            }
            for (JsonElement jsonElement : castJson.getAsJsonArray("effects")) {
                JsonObject effectJson = jsonElement.getAsJsonObject();
                cast.addEffect(aliasGson.fromJson(effectJson, CastEffect.class));
            }
        }
        DisplayCardTemplate card = builder.build();
        DisplayCardTemplate previous = cards.put(id, card);
        if (previous != null) {
            throw new IllegalStateException("Multiple cards registered to same alias: " + alias);
        }
        return card;
    }

    public DisplayMinionTemplate parseMinion(JsonObject minionJson) {
        String alias = minionJson.get("alias").getAsString();
        int id = registerIfAbsent(minionAliases, alias);
        DisplayMinionTemplateBuilder builder = new DisplayMinionTemplateBuilder(id);
        JsonObject display = minionJson.getAsJsonObject("display");
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
            builder.setName("MissingNo #" + id);
            builder.setDescription("404");
            builder.setFlavourText("Nothing here.");
            builder.setImagePath(null);
        }

        JsonObject components = minionJson.getAsJsonObject("components");
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            int component = componentAliases.get(entry.getKey());
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
            throw new UnsupportedOperationException(value.toString());
        }
        DisplayMinionTemplate minion = builder.build();
        DisplayMinionTemplate previous = minions.put(id, minion);
        if (previous != null) {
            throw new IllegalStateException("Multiple minions registered to same alias: " + alias);
        }
        return minion;
    }

    public Set<String> unresolvedCards() {
        return cardAliases.entrySet().stream()
                .filter(e -> !cards.containsKey(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<String> unresolvedMinions() {
        return minionAliases.entrySet().stream()
                .filter(e -> !minions.containsKey(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private int registerIfAbsent(Map<String, Integer> aliases, String alias) {
        int nextId = aliases.size() + 1;
        Integer previousId = aliases.putIfAbsent(alias, nextId);
        if (previousId != null) {
            return previousId;
        }
        return nextId;
    }

}
