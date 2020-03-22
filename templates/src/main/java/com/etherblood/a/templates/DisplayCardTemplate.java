package com.etherblood.a.templates;

import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayCardTemplate extends CardTemplate {

    private final String name, flavourText, description, imagePath;
    private final List<CardColor> colors;

    public DisplayCardTemplate(int templateId, CardCast attackPhaseCast, CardCast blockPhaseCast, String name, String flavourText, String description, String imagePath, List<CardColor> colors) {
        super(templateId, attackPhaseCast, blockPhaseCast);
        this.name = name;
        this.flavourText = flavourText;
        this.description = description;
        this.imagePath = imagePath;
        this.colors = Collections.unmodifiableList(new ArrayList<>(colors));
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
}
