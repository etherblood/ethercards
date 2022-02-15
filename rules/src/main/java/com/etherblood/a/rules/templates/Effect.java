package com.etherblood.ethercards.rules.templates;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.GameTemplates;
import java.util.function.IntUnaryOperator;

public interface Effect {

    void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget);
}
