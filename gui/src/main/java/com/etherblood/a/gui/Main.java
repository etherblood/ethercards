package com.etherblood.a.gui;

import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.jme3.system.AppSettings;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("First argument must be a jwt.");
        }
        Properties properties = new Properties();
        try ( Reader reader = Files.newBufferedReader(Paths.get("config.properties"))) {
            properties.load(reader);
        }
        String jwtPubKeyPath = properties.getProperty("jwtPubKeyPath");
        String jwtUrl = properties.getProperty("jwtUrl");

        JwtParser jwtParser;
        if (jwtPubKeyPath != null) {
            jwtParser = JwtParser.withPublicKeyFile(jwtPubKeyPath);
        } else if (jwtUrl != null) {
            jwtParser = JwtParser.withVerifyUrl(jwtUrl);
        } else {
            throw new IllegalStateException("Jwt verification not initialized.");
        }

        String jwt = args[0];
        JwtAuthentication authentication = jwtParser.verify(jwt);
        System.out.println("Logged in as " + authentication.user.login);

        GameApplication app = new GameApplication(properties, authentication);
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1600);
        settings.setHeight(900);
        settings.setTitle("a");
        settings.setFrameRate(60);
        settings.setAudioRenderer(null);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        app.start();
    }
}
