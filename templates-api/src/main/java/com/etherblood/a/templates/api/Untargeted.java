package com.etherblood.a.templates.api;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.TargetSelection;

public class Untargeted implements TargetSelection {

    private static final IntList EMPTY_LIST = new IntList(0);

    @Override
    public IntList getValidTargets(EntityData data, GameTemplates templates, int source) {
        return EMPTY_LIST;
    }

}
