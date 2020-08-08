package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public class GameLoopService {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;

    public GameLoopService(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
    }

    public void run() {
        ResolveSystem resolveSystem = new ResolveSystem(data, templates, random, events);
        PhaseSystem phaseSystem = new PhaseSystem(data, templates, random, events, resolveSystem);
        CastSystem castSystem = new CastSystem(data, templates, random, events);
        UseAbilitySystem abilitySystem = new UseAbilitySystem(data, templates, random, events);
        
        castSystem.run();
        abilitySystem.run();
        resolveSystem.run();
        phaseSystem.run();
    }

}
