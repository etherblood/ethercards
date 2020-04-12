package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class DrawSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (int player : data.list(core.DRAW_CARDS)) {
            int cards = data.get(player, core.DRAW_CARDS);
            SystemsUtil.drawCards(data, cards, game.getRandom(), player);
            data.remove(player, core.DRAW_CARDS);
        }
    }
}
