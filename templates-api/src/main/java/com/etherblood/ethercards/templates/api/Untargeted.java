package com.etherblood.ethercards.templates.api;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.templates.TargetSelection;

public class Untargeted implements TargetSelection {

    @Override
    public EntityList getValidTargets(EntityData data, GameTemplates templates, int source) {
        return EntityList.EMPTY;
    }

}
