package com.etherblood.a.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.effects.Effect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DisplayCardTemplate extends CardTemplate {

    private final String alias, name, flavourText, description, imagePath;
    private final List<CardColor> colors;

    public DisplayCardTemplate(int templateId, Integer manaCost, CardCast[] casts, String alias, String name, String flavourText, String description, String imagePath, List<CardColor> colors, IntMap components, Set<Tribe> tribes, List<Effect> onCastEffects, List<Effect> onSummonEffects, List<Effect> onDeathEffects, List<Effect> onSurviveEffects, List<Effect> onUpkeepEffects, List<Effect> afterBattleEffects, List<StatModifier> attackModifiers, List<StatModifier> healthModifiers) {
        super(templateId, manaCost, casts, components, tribes, onCastEffects, onSummonEffects, onDeathEffects, onSurviveEffects, onUpkeepEffects, afterBattleEffects, attackModifiers, healthModifiers);
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
