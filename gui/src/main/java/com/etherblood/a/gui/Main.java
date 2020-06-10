package com.etherblood.a.gui;

import com.etherblood.a.client.GameClient;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.jwt.JwtUtils;
import com.etherblood.a.network.api.jwt.Token;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jme3.system.AppSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Function;

public class Main {

    public static void main(String... args) throws Exception {
        String jwt = args[0];
        Token token = JwtUtils.verify(jwt);
        System.out.println("Logged in as " + token.user.login);
        System.out.println("Please enter 0 for a human opponent or select bot strength (1-10)");
        int strength = Integer.parseInt(new Scanner(System.in).nextLine());

        Function<String, JsonObject> assetLoader = x -> {
            try {
                return new Gson().fromJson(Files.newBufferedReader(Paths.get("../assets/templates/" + x)), JsonObject.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        RawLibraryTemplate library = new Gson().fromJson(assetLoader.apply("libraries/default.json"), RawLibraryTemplate.class);

        GameClient client = new GameClient(assetLoader);
        client.start("localhost");
        GameReplayService game;
        if (strength == 0) {
            game = client.requestGame(jwt, library).get();
        } else if (strength > 0) {
            game = client.requestBotgame(jwt, library, strength * 10_000).get();
        } else {
            throw new AssertionError(strength);
        }

        GameApplication app = new GameApplication(game, client::requestMove, game.getPlayerIndex(token.user.id));
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1600);
        settings.setHeight(900);
        settings.setTitle("a");
        settings.setFrameRate(60);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }
}
