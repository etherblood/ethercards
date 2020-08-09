package com.etherblood.a.templates.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
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
                JsonObject template;
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    template = gson.fromJson(reader, JsonObject.class);
                }
                update(template);
                try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    gson.toJson(template, writer);
                    writer.flush();
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error with file " + path, ex);
            }
        });
    }

    private static void update(JsonObject template) {
        int version;
        if (template.has("version")) {
            version = template.get("version").getAsInt();
        } else {
            version = 0;
        }
        switch (version) {
            case 1:
                return;
            case 0:
                break;
            default:
                throw new AssertionError(version);
        }

        template.addProperty("version", 1);

        JsonObject cast = new JsonObject();
        cast.add("manaCost", template.remove("manaCost"));
        cast.add("target", template.remove("target"));
        cast.add("effects", template.remove("cast"));

        JsonObject hand = new JsonObject();
        hand.add("cast", cast);
        hand.add("passive", template.remove("hand"));
        template.add("hand", hand);

        JsonObject battle = new JsonObject();
        battle.add("components", template.remove("components"));
        battle.add("componentModifiers", template.remove("componentModifiers"));
        battle.add("passive", template.remove("battle"));
        template.add("battle", battle);

        JsonObject graveyard = new JsonObject();
        graveyard.add("passive", template.remove("graveyard"));
        template.add("graveyard", graveyard);

        JsonObject library = new JsonObject();
        library.add("passive", template.remove("library"));
        template.add("library", library);
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
