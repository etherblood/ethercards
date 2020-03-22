package com.etherblood.a.gui;

import com.jme3.system.AppSettings;

public class Main {

    public static void main(String... args) {
        CardsApp app = new CardsApp();
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
