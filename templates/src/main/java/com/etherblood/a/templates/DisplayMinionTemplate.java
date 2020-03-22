package com.etherblood.a.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayMinionTemplate extends MinionTemplate {

    private final String name, flavourText, description, imagePath;
    private final List<CardColor> colors;

    public DisplayMinionTemplate(int id, IntMap components, String name, String flavourText, String description, String imagePath, List<CardColor> colors) {
        super(id, components);
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

    public List<CardColor> getColors() {
        return colors;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DisplayMinionTemplate)) {
            return false;
        }
        DisplayMinionTemplate other = (DisplayMinionTemplate) obj;
        return getId() == other.getId();
    }
}
