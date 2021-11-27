package com.etherblood.a.game.server;

import com.destrostudios.authtoken.KeyJwtService;
import com.esotericsoftware.minlog.Log;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        Log.set(Log.LEVEL_DEBUG);
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(Paths.get("config.properties"))) {
            props.load(reader);
        }
        String assetsPath = props.getProperty("assets");
        String version;
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("git.properties")) {
            if (inputStream != null) {
                Properties gitProperties = new Properties();
                gitProperties.load(inputStream);
                version = gitProperties.getProperty("git.commit.id.full");
            } else {
                version = "developer";
            }
        }

        RawLibraryTemplate botLibrary = new RawLibraryTemplate();
        botLibrary.hero = "blitzcrank";
        botLibrary.cards = Arrays.stream(new Gson().fromJson(TemplatesLoader.loadFile(assetsPath + "templates/card_pool.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> 1));
        new GameServer(new KeyJwtService(), x -> TemplatesLoader.loadFile(assetsPath + "templates/cards/" + x + ".json"), botLibrary, version).start();
    }
}
