package com.etherblood.ethercards.rules.templates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.rules.GameTemplates;

import java.util.function.IntUnaryOperator;

public interface TargetSelection {

    //TODO: move this field into template, it is only used for casts
    default boolean requiresTarget() {
        return false;
    }

    default EntityList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, EntityList validTargets) {
        // we select all targets as default, implementations may choose subsets
        assert !requiresTarget();
        if (validTargets.isEmpty()) {
            assert isValidTarget(data, templates, source, null);
        } else {
            for (int validTarget : validTargets) {
                assert isValidTarget(data, templates, source, validTarget);
            }
        }
        return validTargets;
    }

    default boolean isValidTarget(EntityData data, GameTemplates templates, int source, Integer target) {
        EntityList validTargets = getValidTargets(data, templates, source);
        if (target == null) {
            return !requiresTarget() && validTargets.isEmpty();
        }
        return validTargets.contains(target);
    }

    EntityList getValidTargets(EntityData data, GameTemplates templates, int source);
}
