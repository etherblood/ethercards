package com.etherblood.a.templates.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TemplateMigrationMain {

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Path folder = Paths.get("../assets/templates/cards/");
        Files.list(folder).forEach(path -> {
            try {
                File file = path.toFile();
                JsonObject template;
                try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                    template = gson.fromJson(reader, JsonObject.class);
                }
                update(template);
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    gson.toJson(template, writer);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error with file " + path, ex);
            }
        });
    }

    private static void update(JsonObject template) {
        if (template.has("onDeath")) {
            JsonObject zone = template.getAsJsonObject("graveyard");
            if (zone == null) {
                zone = new JsonObject();
                template.add("graveyard", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_SELF_DEATH");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_SELF_DEATH", effects);
            }
            effects.addAll(template.remove("onDeath").getAsJsonArray());
        }
        if (template.has("onSelfMovedToGraveyard")) {
            JsonObject zone = template.getAsJsonObject("graveyard");
            if (zone == null) {
                zone = new JsonObject();
                template.add("graveyard", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_SELF_ENTER_GRAVEYARD");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_SELF_ENTER_GRAVEYARD", effects);
            }
            effects.addAll(template.remove("onSelfMovedToGraveyard").getAsJsonArray());
        }
        
        
        if (template.has("onSurvive")) {
            JsonObject zone = template.getAsJsonObject("battle");
            if (zone == null) {
                zone = new JsonObject();
                template.add("battle", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_SELF_SURVIVE");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_SELF_SURVIVE", effects);
            }
            effects.addAll(template.remove("onSurvive").getAsJsonArray());
        }
        if (template.has("onUpkeep")) {
            JsonObject zone = template.getAsJsonObject("battle");
            if (zone == null) {
                zone = new JsonObject();
                template.add("battle", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_OWNER_UPKEEP");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_OWNER_UPKEEP", effects);
            }
            effects.addAll(template.remove("onUpkeep").getAsJsonArray());
        }
        if (template.has("afterBattle")) {
            JsonObject zone = template.getAsJsonObject("battle");
            if (zone == null) {
                zone = new JsonObject();
                template.add("battle", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_SELF_FIGHT");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_SELF_FIGHT", effects);
            }
            effects.addAll(template.remove("afterBattle").getAsJsonArray());
        }
        if (template.has("onCast")) {
            JsonObject zone = template.getAsJsonObject("battle");
            if (zone == null) {
                zone = new JsonObject();
                template.add("battle", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_OTHER_CAST");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_OTHER_CAST", effects);
            }
            effects.addAll(template.remove("onCast").getAsJsonArray());
        }
        if (template.has("onSummon")) {
            JsonObject zone = template.getAsJsonObject("battle");
            if (zone == null) {
                zone = new JsonObject();
                template.add("battle", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_OTHER_SUMMON");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_OTHER_SUMMON", effects);
            }
            effects.addAll(template.remove("onSummon").getAsJsonArray());
        }
        if (template.has("onDraw")) {
            JsonObject zone = template.getAsJsonObject("battle");
            if (zone == null) {
                zone = new JsonObject();
                template.add("battle", zone);
            }
            JsonArray effects = zone.getAsJsonArray("TRIGGER_OWNER_DRAW");
            if (effects == null) {
                effects = new JsonArray();
                zone.add("TRIGGER_OWNER_DRAW", effects);
            }
            effects.addAll(template.remove("onDraw").getAsJsonArray());
        }
    }

    private static void updateRecursively(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonArray()) {
                updateRecursively(value.getAsJsonArray());
            }
            if (value.isJsonObject()) {
                updateRecursively(value.getAsJsonObject());
            }
        }
        
        //update here
    }

    private static void updateRecursively(JsonArray array) {
        for (JsonElement value : array) {
            if (value.isJsonArray()) {
                updateRecursively(value.getAsJsonArray());
            }
            if (value.isJsonObject()) {
                updateRecursively(value.getAsJsonObject());
            }
        }
    }

}
