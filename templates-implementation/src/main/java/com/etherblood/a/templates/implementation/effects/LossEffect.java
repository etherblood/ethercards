package com.etherblood.ethercards.templates.implementation.effects;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.PlayerResult;
import com.etherblood.ethercards.rules.templates.Effect;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class LossEffect implements Effect {

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        SystemsUtil.setPlayerResult(data, target, PlayerResult.LOSS);
    }
}
