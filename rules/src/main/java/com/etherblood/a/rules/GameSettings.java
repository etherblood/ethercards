package com.etherblood.a.rules;

import com.etherblood.a.entities.Components;

public class GameSettings {

    public final GameTemplates templates;
    public final Components components;

    public GameSettings(GameTemplates templates, Components components) {
        this.templates = templates;
        this.components = components;
    }

}
