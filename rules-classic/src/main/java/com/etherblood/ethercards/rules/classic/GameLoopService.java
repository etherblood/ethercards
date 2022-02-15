package com.etherblood.ethercards.rules.classic;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.updates.systems.CastSystem;
import com.etherblood.ethercards.rules.updates.systems.UseAbilitySystem;
import java.util.function.IntUnaryOperator;

public class GameLoopService implements Runnable {

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

    @Override
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
