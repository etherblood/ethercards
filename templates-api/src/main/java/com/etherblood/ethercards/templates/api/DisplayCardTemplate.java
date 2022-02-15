package com.etherblood.ethercards.templates.api;

import com.etherblood.ethercards.rules.templates.CardTemplate;
import com.etherblood.ethercards.rules.templates.Tribe;
import com.etherblood.ethercards.rules.templates.ZoneState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DisplayCardTemplate extends CardTemplate {

    private final String alias, name, flavourText, description, imagePath;
    private final int frames, loopMillis;
    private final List<CardColor> colors;

    public DisplayCardTemplate(int templateId, boolean isMinion, String alias, String name, String flavourText, String description, String imagePath, int frames, int loopMillis, List<CardColor> colors, Set<Tribe> tribes, ZoneState hand, ZoneState battle, ZoneState graveyard, ZoneState library) {
        super(templateId, isMinion, tribes, hand, battle, graveyard, library);
        this.alias = alias;
        this.name = name;
        this.flavourText = flavourText;
        this.description = description;
        this.imagePath = imagePath;
        this.frames = frames;
        this.loopMillis = loopMillis;
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

    public int getFrames() {
        return frames;
    }

    public int getLoopMillis() {
        return loopMillis;
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
