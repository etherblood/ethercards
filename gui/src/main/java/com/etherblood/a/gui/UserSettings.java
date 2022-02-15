package com.etherblood.ethercards.gui;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class UserSettings {

    private static final String USER_SETTINGS_PATH = "settings.props";
    private static final String IS_FULLSCREEN = "screen.fullscreen";
    private static final String SCREEN_WIDTH = "screen.width";
    private static final String SCREEN_HEIGHT = "screen.height";
    private static final UserSettings INSTANCE = new UserSettings();

    private final Properties properties;

    private UserSettings() {
        Properties defaults = new Properties();
        defaults.setProperty(IS_FULLSCREEN, Boolean.toString(false));
        defaults.setProperty(SCREEN_WIDTH, Integer.toString(1600));
        defaults.setProperty(SCREEN_HEIGHT, Integer.toString(900));
        properties = new Properties(defaults);
    }

    public static UserSettings instance() {
        return INSTANCE;
    }

    public void load() throws IOException {
        Path userSettingsPath = Paths.get(USER_SETTINGS_PATH);
        if (userSettingsPath.toFile().exists()) {
            try (Reader reader = Files.newBufferedReader(userSettingsPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }
    }

    public void save() throws IOException {
        Path userSettingsPath = Paths.get(USER_SETTINGS_PATH);
        try (Writer writer = Files.newBufferedWriter(userSettingsPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            properties.store(writer, null);
        }
    }

    public boolean isFullscreen() {
        return getBoolean(IS_FULLSCREEN);
    }

    public void setFullscreen(boolean value) {
        setBoolean(IS_FULLSCREEN, value);
    }

    public int getScreenWidth() {
        return getInt(SCREEN_WIDTH);
    }

    public void setScreenWidth(int value) {
        setInt(SCREEN_WIDTH, value);
    }

    public int getScreenHeight() {
        return getInt(SCREEN_HEIGHT);
    }

    public void setScreenHeight(int value) {
        setInt(SCREEN_HEIGHT, value);
    }

    private int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    private void setInt(String key, int value) {
        properties.setProperty(key, Integer.toString(value));
    }

    private boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    private void setBoolean(String key, boolean value) {
        properties.setProperty(key, Boolean.toString(value));
    }
}
