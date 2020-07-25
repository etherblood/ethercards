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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
        if (true) {
            return;
        }
        updateObject(template);
    }

    private static void updateObject(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonArray()) {
                updateArray(value.getAsJsonArray());
            }
            if (value.isJsonObject()) {
                updateObject(value.getAsJsonObject());
            }
        }
        if (object.has("type") && object.has("targets")) {
            String type = object.get("type").getAsString();
            JsonElement prevTargets = object.get("targets");
            if (type.equals("targeted") && prevTargets.isJsonArray()) {
                JsonElement filters = object.remove("targets");
                JsonElement select = object.remove("targeting");

                JsonObject targets = new JsonObject();
                targets.addProperty("type", "simple");
                targets.add("select", select);
                targets.add("filters", filters);
                object.add("targets", targets);
            }
        }
    }

    private static void updateArray(JsonArray array) {
        for (JsonElement value : array) {
            if (value.isJsonArray()) {
                updateArray(value.getAsJsonArray());
            }
            if (value.isJsonObject()) {
                updateObject(value.getAsJsonObject());
            }
        }
    }

}
