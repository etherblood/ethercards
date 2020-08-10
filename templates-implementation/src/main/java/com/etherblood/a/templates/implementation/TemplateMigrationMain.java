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
import java.util.function.Consumer;

public class TemplateMigrationMain {

    public static void main(String[] args) throws IOException {
        Path folder = Paths.get("../assets/templates/cards/");

        runForTemplates(folder, TemplateMigrationMain::markAsOutdated);
//        runForTemplates(folder, TemplateMigrationMain::update);
    }

    private static void runForTemplates(Path folder, Consumer<JsonObject> updater) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Files.list(folder).forEach(path -> {
            try {
                JsonObject template;
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    template = gson.fromJson(reader, JsonObject.class);
                }
                updater.accept(template);
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
        template.remove("outdated");
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

    private static void markAsOutdated(JsonObject template) {
        template.addProperty("outdated", true);
    }

}
