package com.etherblood.a.server;

import com.esotericsoftware.minlog.Log;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.load(Files.newBufferedReader(Paths.get("config.properties")));
        String jwtPubKeyPath = props.getProperty("jwtPubKeyPath");
        String jwtUrl = props.getProperty("jwtUrl");

        JwtParser jwtParser;
        if (jwtPubKeyPath != null) {
            jwtParser = JwtParser.withPublicKeyFile(jwtPubKeyPath);
        } else if (jwtUrl != null) {
            jwtParser = JwtParser.withVerifyUrl(jwtUrl);
        } else {
            throw new IllegalStateException("Jwt verification not initialized.");
        }

        Function<String, JsonElement> assetLoader = x -> {
            try {
                return new Gson().fromJson(Files.newBufferedReader(Paths.get(props.getProperty("assets") + "templates/" + x)), JsonElement.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        Log.set(Log.LEVEL_INFO);
        new GameServer(jwtParser, assetLoader).start();
    }
}
