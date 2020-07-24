package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.templates.Effect;
import java.util.function.IntUnaryOperator;

public class SourceOwnerPhaseEffects implements Effect {

    public final Effect[] attack;
    public final Effect[] block;

    public SourceOwnerPhaseEffects(Effect[] attack, Effect[] block) {
        this.attack = attack;
        this.block = block;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int sourceOwner = data.get(source, core.OWNER);
        if (data.hasValue(sourceOwner, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK)) {
            for (Effect effect : attack) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
        if (data.hasValue(sourceOwner, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK)) {
            for (Effect effect : block) {
                effect.apply(data, templates, random, events, source, target);
            }
        }
    }
}
