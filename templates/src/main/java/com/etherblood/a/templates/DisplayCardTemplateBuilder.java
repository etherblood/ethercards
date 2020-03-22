package com.etherblood.a.templates;

import com.etherblood.a.rules.templates.CardTemplateBuilder;
import java.util.List;

public class DisplayCardTemplateBuilder extends CardTemplateBuilder {

    private String name, flavourText, description, imagePath;
    private List<CardColor> colors;

    public DisplayCardTemplateBuilder(int templateId) {
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
    public DisplayCardTemplate build() {
        return new DisplayCardTemplate(templateId, attackPhaseCast.build(), blockPhaseCast.build(), name, flavourText, description, imagePath, colors);
    }
}
