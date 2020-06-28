package com.etherblood.a.templates;

import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayCardTemplate extends CardTemplate {

    private final String alias, name, flavourText, description, imagePath;
    private final DisplayStats displayStats;
    private final List<CardColor> colors;

    public DisplayCardTemplate(int templateId, CardCast[] casts, String alias, String name, String flavourText, String description, String imagePath, List<CardColor> colors, DisplayStats displayMinionStats) {
        super(templateId, casts);
        this.alias = alias;
        this.name = name;
        this.flavourText = flavourText;
        this.description = description;
        this.imagePath = imagePath;
        this.colors = Collections.unmodifiableList(new ArrayList<>(colors));
        this.displayStats = displayMinionStats;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public String getFlavourText() {
        return flavourText;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public List<CardColor> getColors() {
        return colors;
    }

    @Override
    public String getTemplateName() {
        return name;
    }

    public DisplayStats getDisplayStats() {
        return displayStats;
    }

    @Override
    public String toString() {
        return "DisplayCardTemplate{" + "alias=" + alias + '}';
    }
}
