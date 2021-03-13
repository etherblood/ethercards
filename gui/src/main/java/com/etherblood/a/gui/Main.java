package com.etherblood.a.gui;

import com.destrostudios.authtoken.JwtAuthentication;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.jme3.system.AppSettings;
import com.jme3.system.ErrorDialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {

    public static void main(String... args) {
        try {
            startApp(args);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            ErrorDialog dialog = new ErrorDialog(sw.toString());
            dialog.setVisible(true);
        }
    }

    private static void startApp(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("First argument must be a jwt.");
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Paths.get("config.properties"), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        String version;
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("git.properties")) {
            Properties gitProperties = new Properties();
            gitProperties.load(inputStream);
            version = gitProperties.getProperty("git.commit.id.full");
        }

        String jwt = args[0];
        JwtAuthentication authentication = new NoValidateJwtService().decode(jwt);
        System.out.println("Logged in as " + authentication.user.login);

        UserSettings userSettings = UserSettings.instance();
        userSettings.load();

        GameApplication app = new GameApplication(properties, authentication, version);
        AppSettings settings = new AppSettings(true);
        settings.setResizable(true);
        settings.setFullscreen(userSettings.isFullscreen());
        settings.setWidth(userSettings.isFullscreen() ? -1 : userSettings.getScreenWidth());
        settings.setHeight(userSettings.isFullscreen() ? -1 : userSettings.getScreenHeight());
        settings.setTitle("Ethercards");
        settings.setFrameRate(60);
        settings.setAudioRenderer(null);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        app.start();
    }
}
