package com.etherblood.a.templates.api;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.TargetSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisplayCardTemplate extends CardTemplate {

    private final String alias, name, flavourText, description, imagePath;
    private final List<CardColor> colors;

    public DisplayCardTemplate(int templateId, boolean isMinion, Integer manaCost, TargetSelection castTarget, List<Effect> castEffects, String alias, String name, String flavourText, String description, String imagePath, List<CardColor> colors, IntMap components, Set<Tribe> tribes, List<Effect> onCastEffects, List<Effect> onSummonEffects, List<Effect> onDeathEffects, List<Effect> onSurviveEffects, List<Effect> onUpkeepEffects, List<Effect> afterBattleEffects, List<Effect> onDrawEffects, Map<Integer, List<StatModifier>> componentModifiers) {
        super(templateId, isMinion, manaCost, castTarget, castEffects, components, tribes, onCastEffects, onSummonEffects, onDeathEffects, onSurviveEffects, onUpkeepEffects, afterBattleEffects, onDrawEffects, componentModifiers);
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
