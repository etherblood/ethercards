package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class HasSourceTeamPredicate implements TargetPredicate {

    private final boolean hasSourceTeam;

    public HasSourceTeamPredicate(boolean hasSourceTeam) {
        this.hasSourceTeam = hasSourceTeam;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int team = data.get(source, core.TEAM);
        return data.hasValue(target, core.TEAM, team) == hasSourceTeam;
    }
}
