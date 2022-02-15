package com.etherblood.ethercards.templates.api;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;

public interface TargetPredicate {

    boolean test(EntityData data, GameTemplates templates, int source, int target);
}
