package com.etherblood.a.templates.api;

import com.etherblood.a.rules.templates.CardTemplateBuilder;
import java.util.List;

public class DisplayCardTemplateBuilder extends CardTemplateBuilder {

    private String alias, name, flavourText, description, imagePath;
    private List<CardColor> colors;

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getAlias() {
        return alias;
    }

    @Override
    public DisplayCardTemplate build(int id) {
        return new DisplayCardTemplate(id, !components.isEmpty(), manaCost, castTarget, castEffects, alias, name, flavourText, description, imagePath, colors, components, tribes, inBattle, inHand, inLibrary, inGraveyard, componentModifiers, battleAbility);
    }
}
