package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

/**
 *
 * @author Philipp
 */
public interface TargetSelection {

    //TODO: move this field into template, it is only used for casts
    default boolean requiresTarget() {
        return false;
    }

    default IntList selectTargets(EntityData data, GameTemplates templates, IntUnaryOperator random, int source, IntList validTargets) {
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
        IntList validTargets = getValidTargets(data, templates, source);
        if (target == null) {
            return !requiresTarget() && validTargets.isEmpty();
        }
        return validTargets.contains(target);
    }

    IntList getValidTargets(EntityData data, GameTemplates templates, int source);
}
