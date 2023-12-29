package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.templates.TargetSelection;

import java.util.function.IntUnaryOperator;

public class TargetedEffects implements Effect {

    public final TargetSelection targets;
    public final Effect[] effects;

    public TargetedEffects(TargetSelection targets, Effect[] effects) {
        this.targets = targets;
        this.effects = effects;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        EntityList availableTargets = targets.getValidTargets(data, templates, source);
        if (availableTargets.isEmpty()) {
            return;
        }
        for (int newTarget : targets.selectTargets(data, templates, random, source, availableTargets)) {
            for (Effect effect : effects) {
                effect.apply(data, templates, random, events, source, newTarget);
            }
        }
    }
}
