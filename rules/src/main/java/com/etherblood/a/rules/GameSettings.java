package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.entities.Components;

public class GameSettings {

    public final GameTemplates templates;
    public final Components components;

    public GameSettings(GameTemplates templates, Components components) {
        this.templates = templates;
        this.components = components;
    }

}
