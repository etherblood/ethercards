package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DeathSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DeathSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        IntList lossCandidates = new IntList();
        for (int entity : data.list(Components.DIE)) {
            LOG.info("{} died.", entityLog(entity));
            int owner = data.get(entity, Components.OWNED_BY);
            if (!lossCandidates.contains(owner)) {
                lossCandidates.add(owner);
            }
            data.remove(entity, Components.IN_BATTLE_ZONE);
            data.remove(entity, Components.OWNED_BY);
            data.remove(entity, Components.DIE);
        }
        for (int entity : data.list(Components.IN_BATTLE_ZONE)) {
            int owner = data.get(entity, Components.OWNED_BY);
            lossCandidates.swapRemove(owner);
        }
        for (int player : lossCandidates) {
            if (!data.has(player, Components.HAS_LOST)) {
                data.set(player, Components.HAS_LOST, 1);
                LOG.info("{} lost.", entityLog(player));

                int next = data.get(player, Components.NEXT_PLAYER);
                for (int turnPlayer : data.list(Components.NEXT_PLAYER)) {
                    int turnNext = data.get(turnPlayer, Components.NEXT_PLAYER);
                    if (turnNext == player) {
                        data.set(turnPlayer, Components.NEXT_PLAYER, next);
                    }
                }
            }
        }
    }
}
