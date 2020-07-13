package com.etherblood.a.server;

import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        try ( Reader reader = Files.newBufferedReader(Paths.get("config.properties"))) {
            props.load(reader);
        }
        String jwtPubKeyPath = props.getProperty("jwtPubKeyPath");
        String jwtUrl = props.getProperty("jwtUrl");
        String assetsPath = props.getProperty("assets");

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
        new GameServer(jwtParser, x -> TemplatesLoader.loadFile(assetsPath + "templates/cards/" + x + ".json"), botLibrary).start();
    }
}
