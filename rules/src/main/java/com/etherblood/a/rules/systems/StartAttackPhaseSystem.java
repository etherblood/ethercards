package com.etherblood.a.rules.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.AbstractSystem;
import com.etherblood.a.rules.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class StartAttackPhaseSystem extends AbstractSystem {

    private static final Logger LOG = LoggerFactory.getLogger(StartAttackPhaseSystem.class);

    @Override
    public void run(EntityData data, Random random) {
        IntList entities = data.list(Components.START_ATTACK_PHASE);
        for (int player : entities) {
            data.remove(player, Components.START_ATTACK_PHASE);
            data.set(player, Components.IN_ATTACK_PHASE, 1);
            LOG.debug("{} started {}.",
                    entityLog(player),
                    componentLog(Components.IN_ATTACK_PHASE, 1));

            int mana = 0;
            int draws = data.getOptional(player, Components.DRAW_CARDS).orElse(0);
            draws++;
            for (int entity : data.list(Components.IN_BATTLE_ZONE).stream()
                    .filter(x -> data.hasValue(x, Components.OWNED_BY, player))
                    .toArray()) {
                data.getOptional(entity, Components.TIRED).ifPresent(tiredness -> {
                    tiredness--;
                    if (tiredness > 0) {
                        data.set(entity, Components.TIRED, tiredness);
                        LOG.debug("{} is {}.", entityLog(entity), componentLog(Components.TIRED, tiredness));
                    } else {
                        data.remove(entity, Components.TIRED);
                        LOG.info("{} is {}.", entityLog(entity), componentLog(Components.TIRED, 0));
                    }
                });
                data.getOptional(entity, Components.MANA_GROWTH).ifPresent(growth -> {
                    int manaPool = data.getOptional(entity, Components.MANA_POOL).orElse(0);
                    manaPool += growth;
                    data.set(entity, Components.MANA_POOL, manaPool);
                    LOG.info("{} has {}.",
                            entityLog(entity),
                            componentLog(Components.MANA_POOL, manaPool));
                });
                mana += data.getOptional(entity, Components.MANA_POOL).orElse(0);
                draws += data.getOptional(entity, Components.DRAWS_PER_TURN).orElse(0);
            }
            mana = Math.max(mana, 0);
            data.set(player, Components.MANA, mana);
            LOG.debug("{} has {}.",
                    entityLog(player),
                    componentLog(Components.MANA, mana));

            draws = Math.max(draws, 0);
            data.set(player, Components.DRAW_CARDS, draws);
        }
    }
}
