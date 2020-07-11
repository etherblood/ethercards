package com.etherblood.a.rules.updates;

import com.etherblood.a.rules.updates.systems.DamageSystem;
import com.etherblood.a.rules.updates.systems.DeathSystem;
import com.etherblood.a.rules.updates.systems.CastSystem;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.systems.DiscardSystem;
import com.etherblood.a.rules.updates.systems.DrawSystem;
import com.etherblood.a.rules.updates.systems.EndPhaseSystem;
import com.etherblood.a.rules.updates.systems.PlayerResultSystem;
import com.etherblood.a.rules.updates.systems.StartPhaseSystem;
import com.etherblood.a.rules.updates.systems.SurvivalSystem;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public class SystemFactory {

    private final EntityData data;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;

    public SystemFactory(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.templates = templates;
        this.random = random;
        this.events = events;
    }

    public UpdateService build() {
        return new UpdateService(
                Arrays.asList(
                        new EndPhaseSystem(data, templates, random, events),
                        new StartPhaseSystem(data, templates, random, events)
                ),
                Arrays.asList(
                        new CastSystem(data, templates, random, events),
                        new DiscardSystem(data, random),
                        new DrawSystem(data, templates, events, random),
                        new DamageSystem(data, templates, random, events),
                        new DeathSystem(data, templates, random, events),
                        new PlayerResultSystem(data)
                ),
                new SurvivalSystem(data, templates, random, events),
                new EffectiveStatsService(data, templates)
        );
    }
}
