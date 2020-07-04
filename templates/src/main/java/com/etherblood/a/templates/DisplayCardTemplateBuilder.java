package com.etherblood.a.templates;

import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardCastBuilder;
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

    @Override
    public DisplayCardTemplate build(int id) {
        return new DisplayCardTemplate(id, manaCost, casts.stream().map(CardCastBuilder::build).toArray(CardCast[]::new), alias, name, flavourText, description, imagePath, colors, components, tribes, onCastEffects, onSummonEffects, onDeathEffects, onSurviveEffects, onUpkeepEffects, afterBattleEffects);
    }
}
