package com.etherblood.ethercards.gui;

import com.etherblood.ethercards.network.api.RecordTypeAdapterFactory;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryIO {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static RawLibraryTemplate load(String name) {
        Path path = Paths.get(name);
        if (!path.toFile().isFile()) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, RawLibraryTemplate.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void store(String name, RawLibraryTemplate library) {
        assert library != null;
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(name))) {
            GSON.toJson(library, writer);
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
