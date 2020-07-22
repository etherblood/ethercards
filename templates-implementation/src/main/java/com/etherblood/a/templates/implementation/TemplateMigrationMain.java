package com.etherblood.a.templates.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TemplateMigrationMain {

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path folder = Paths.get("../assets/templates/cards/");
        Files.list(folder).forEach(path -> {
            try {
                File file = path.toFile();
                JsonObject template;
                try (Reader reader = new BufferedReader(new FileReader(file))) {
                    template = gson.fromJson(reader, JsonObject.class);
                }
                update(template);
                try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                    gson.toJson(template, writer);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error with file " + path, ex);
            }
        });
    }

    private static void update(JsonObject template) {
        if (true) {
            return;
        }
        JsonArray casts = template.getAsJsonArray("casts");
        if (casts != null) {
            if (casts.size() > 1) {
                throw new IllegalStateException();
            }
            for (JsonElement castElement : casts) {
                JsonObject cast = castElement.getAsJsonObject();
                if (template.has("cast")) {
                    throw new IllegalStateException();
                }
                template.add("cast", cast.get("effects"));
                if (!template.has("manaCost")) {
                    template.add("manaCost", cast.get("manaCost"));
                }
                if (cast.has("targets")) {
                    boolean requiresTarget = cast.has("targetOptional") && !cast.get("targetOptional").getAsBoolean();

                    JsonObject target = new JsonObject();
                    target.addProperty("type", "simple");
                    target.addProperty("requiresTarget", requiresTarget);
                    target.add("filters", cast.get("targets"));
                    template.add("target", target);
                }
            }
            template.remove("casts");
        }
    }

}
