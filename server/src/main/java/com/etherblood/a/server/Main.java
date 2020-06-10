package com.etherblood.a.server;

import com.esotericsoftware.minlog.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) throws IOException {
        Function<String, JsonObject> assetLoader = x -> {
            try {
                return new Gson().fromJson(Files.newBufferedReader(Paths.get("../assets/templates/" + x)), JsonObject.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        Log.set(Log.LEVEL_INFO);
        new GameServer(assetLoader).start();
    }
}
