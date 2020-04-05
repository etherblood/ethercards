package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.systems.util.SystemsUtil;

public class DrawSystem extends AbstractSystem {

    @Override
    public void run(Game game, EntityData data) {
        for (int player : data.list(Components.DRAW_CARDS)) {
            int cards = data.get(player, Components.DRAW_CARDS);
            SystemsUtil.drawCards(data, cards, game.getRandom(), player);
            data.remove(player, Components.DRAW_CARDS);
        }
    }
}
