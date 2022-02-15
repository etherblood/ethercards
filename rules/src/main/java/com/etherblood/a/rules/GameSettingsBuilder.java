package com.etherblood.ethercards.rules;

import com.etherblood.ethercards.entities.Components;

public class GameSettingsBuilder {

    public GameTemplates templates;
    public Components components;

    public GameSettingsBuilder() {
    }

    public GameSettingsBuilder(GameSettings settings) {
        templates = settings.templates;
        components = settings.components;
    }

    public GameSettings build() {
        return new GameSettings(templates, components);
    }

}
