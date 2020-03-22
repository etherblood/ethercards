package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DrawSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DrawSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        for (int player : data.list(Components.DRAW_CARDS)) {
            int cards = data.get(player, Components.DRAW_CARDS);
            for (int i = 0; i < cards; i++) {
                SystemsUtil.drawCard(data, random, player);
            }
            data.remove(player, Components.DRAW_CARDS);
        }
    }
}
