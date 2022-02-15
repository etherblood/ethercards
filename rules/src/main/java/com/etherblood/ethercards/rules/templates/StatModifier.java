package com.etherblood.ethercards.rules.templates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.rules.GameTemplates;

public interface StatModifier {

    int modify(EntityData data, GameTemplates templates, int self, int target, int stat);
}
