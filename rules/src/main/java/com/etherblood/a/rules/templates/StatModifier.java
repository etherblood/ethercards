package com.etherblood.a.rules.templates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;

public interface StatModifier {

    int modify(EntityData data, GameTemplates templates, int self, int target, int stat);
}
