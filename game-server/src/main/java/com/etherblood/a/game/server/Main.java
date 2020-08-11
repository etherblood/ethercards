package com.etherblood.a.game.server;

import com.esotericsoftware.minlog.Log;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        Log.set(Log.LEVEL_DEBUG);
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(Paths.get("config.properties"))) {
            props.load(reader);
        }
        String jwtPubKeyPath = props.getProperty("jwtPubKeyPath");
        String jwtUrl = props.getProperty("jwtUrl");
        String assetsPath = props.getProperty("assets");
        String version;
        try (Scanner in = new Scanner(Files.newBufferedReader(Paths.get("version.txt"), StandardCharsets.UTF_8))) {
            version = in.nextLine();
        }

        JwtParser jwtParser;
        if (jwtPubKeyPath != null) {
            jwtParser = JwtParser.withPublicKeyFile(jwtPubKeyPath);
        } else if (jwtUrl != null) {
            jwtParser = JwtParser.withVerifyUrl(jwtUrl);
        } else {
            throw new IllegalStateException("Jwt verification not initialized.");
        }

        RawLibraryTemplate botLibrary = new RawLibraryTemplate();
        botLibrary.hero = "elderwood_ahri";
        botLibrary.cards = Arrays.stream(new Gson().fromJson(TemplatesLoader.loadFile(assetsPath + "templates/card_pool.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> 1));
        new GameServer(jwtParser, x -> TemplatesLoader.loadFile(assetsPath + "templates/cards/" + x + ".json"), botLibrary, version).start();
    }
}