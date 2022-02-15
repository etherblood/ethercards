package com.etherblood.ethercards.templates.api;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.TargetSelection;

public class Untargeted implements TargetSelection {

    private static final IntList EMPTY_LIST = new IntList(0);

    @Override
    public IntList getValidTargets(EntityData data, GameTemplates templates, int source) {
        return EMPTY_LIST;
    }

}
