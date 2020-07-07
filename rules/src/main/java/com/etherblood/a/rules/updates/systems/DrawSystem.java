package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Modifier;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class DrawSystem implements ActionSystem {
    
    private final EntityData data;
    private final CoreComponents core;
    private final IntUnaryOperator random;
    private final Modifier[] modifiers;
    private final Trigger[] triggers;
    
    public DrawSystem(EntityData data, IntUnaryOperator random) {
        this.data = data;
        this.random = random;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.modifiers = new Modifier[0];
        this.triggers = new Trigger[0];
    }
    
    @Override
    public boolean isActive() {
        return data.list(core.DRAW_CARDS_REQUEST).nonEmpty();
    }
    
    @Override
    public void before() {
        for (int entity : data.list(core.DRAW_CARDS_REQUEST)) {
            int cards = data.get(entity, core.DRAW_CARDS_REQUEST);
            for (int i = 0; cards > 0 && i < modifiers.length; i++) {
                cards = modifiers[i].modify(entity, cards);
            }
            if (cards > 0) {
                data.set(entity, core.DRAW_CARDS_ACTION, cards);
            }
            data.remove(entity, core.DRAW_CARDS_REQUEST);
        }
    }
    
    @Override
    public void run() {
        for (int player : data.list(core.DRAW_CARDS_ACTION)) {
            int cards = data.get(player, core.DRAW_CARDS_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(player, cards);
            }
        }
    }
    
    @Override
    public void after() {
        for (int player : data.list(core.DRAW_CARDS_ACTION)) {
            int cards = data.get(player, core.DRAW_CARDS_ACTION);
            SystemsUtil.drawCards(data, cards, random, player);
            data.remove(player, core.DRAW_CARDS_ACTION);
        }
        
    }
}
