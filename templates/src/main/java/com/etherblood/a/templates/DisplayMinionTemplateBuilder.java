package com.etherblood.a.templates;

import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.templates.MinionTemplateBuilder;
import java.util.Collections;
import java.util.List;

public class DisplayMinionTemplateBuilder extends MinionTemplateBuilder {

    private String alias, name, flavourText, description, imagePath;
    private List<CardColor> colors = Collections.emptyList();

    public DisplayMinionTemplateBuilder(CoreComponents core) {
        super(core);
    }

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
    public DisplayMinionTemplate build(int templateId) {
        components.set(core.MINION_TEMPLATE, templateId);
        return new DisplayMinionTemplate(templateId, components, tribes, onCastEffects, onSummonEffects, onDeathEffects, onSurviveEffects, onUpkeepEffects, afterBattleEffects, alias, name, flavourText, description, imagePath, colors);
    }
}
