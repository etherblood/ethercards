package com.etherblood.a.templates;

import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.templates.MinionTemplateBuilder;
import java.util.Collections;
import java.util.List;

public class DisplayMinionTemplateBuilder extends MinionTemplateBuilder {

    private String name, flavourText, description, imagePath;
    private List<CardColor> colors = Collections.emptyList();

    public DisplayMinionTemplateBuilder(int templateId) {
        super(templateId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFlavourText(String flavourText) {
        this.flavourText = flavourText;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setColors(List<CardColor> colors) {
        this.colors = colors;
    }

    @Override
    public DisplayMinionTemplate build() {
        components.set(Components.MINION_TEMPLATE, id);
        return new DisplayMinionTemplate(id, components, name, flavourText, description, imagePath, colors);
    }
}
