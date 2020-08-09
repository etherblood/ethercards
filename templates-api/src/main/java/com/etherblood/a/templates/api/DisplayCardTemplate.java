package com.etherblood.a.templates.api;

import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.ZoneState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DisplayCardTemplate extends CardTemplate {

    private final String alias, name, flavourText, description, imagePath;
    private final List<CardColor> colors;

    public DisplayCardTemplate(int templateId, boolean isMinion, String alias, String name, String flavourText, String description, String imagePath, List<CardColor> colors, Set<Tribe> tribes, ZoneState hand, ZoneState battle, ZoneState graveyard, ZoneState library) {
        super(templateId, isMinion, tribes, hand, battle, graveyard, library);
        this.alias = alias;
        this.name = name;
        this.flavourText = flavourText;
        this.description = description;
        this.imagePath = imagePath;
        this.colors = Collections.unmodifiableList(new ArrayList<>(colors));
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

    @Override
    public String toString() {
        return "DisplayCardTemplate{" + "alias=" + alias + '}';
    }
}
