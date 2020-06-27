package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class EndPhaseSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final Trigger[] triggers;

    public EndPhaseSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
        triggers = new Trigger[]{
            (player, phase) -> {
                switch (phase) {
                    case PlayerPhase.BLOCK: {
                        endBlockPhase(player);
                        break;
                    }
                    case PlayerPhase.ATTACK: {
                        endAttackPhase(player);
                        break;
                    }
                    case PlayerPhase.MULLIGAN: {
                        endMulliganPhase(player);
                        break;
                    }
                    case PlayerPhase.BATTLE: {
                        endBattlePhase(player);
                        break;
                    }
                }
            }};
    }

    private void endBlockPhase(int player) {
        data.set(player, core.START_PHASE_REQUEST, PlayerPhase.BATTLE);
    }

    private void endAttackPhase(int player) {
        for (int attacker : data.list(core.ATTACKS_TARGET)) {
            if (!data.hasValue(attacker, core.OWNED_BY, player)) {
                continue;
            }
            if (!data.has(attacker, core.VIGILANCE)) {
                data.set(attacker, core.TIRED, 1);
            }
            int target = data.get(attacker, core.ATTACKS_TARGET);
            if (!data.has(target, core.IN_BATTLE_ZONE)) {
                data.remove(attacker, core.ATTACKS_TARGET);
                continue;
            }
            data.set(target, core.TIRED, 1);
            data.getOptional(attacker, core.DRAWS_ON_ATTACK).ifPresent(draws -> {
                int owner = data.get(attacker, core.OWNED_BY);
                SystemsUtil.increase(data, owner, core.DRAW_CARDS_REQUEST, draws);
            });
            data.getOptional(attacker, core.GIVE_DRAWS_ON_ATTACK).ifPresent(draws -> {
                int targetOwner = data.get(target, core.OWNED_BY);
                SystemsUtil.increase(data, targetOwner, core.DRAW_CARDS_REQUEST, draws);
            });
        }
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.hasValue(minion, core.OWNED_BY, player)) {
                SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.SUMMONING_SICKNESS, 1);
            }
        }

        Integer nextPlayer = SystemsUtil.nextPlayer(data, player);
        if (nextPlayer != null) {
            data.set(nextPlayer, core.START_PHASE_REQUEST, PlayerPhase.BLOCK);
        }
    }

    private void endMulliganPhase(int player) {
        //mulligan logic happens in triggerAndClean
    }

    private void endBattlePhase(int player) {
        for (int attacker : data.list(core.ATTACKS_TARGET)) {
            int attackTarget = data.get(attacker, core.ATTACKS_TARGET);
            if (data.hasValue(attackTarget, core.OWNED_BY, player)) {
                if (data.has(attackTarget, core.IN_BATTLE_ZONE)) {
                    SystemsUtil.fight(data, templates, random, attacker, attackTarget, events);

                    data.getOptional(attackTarget, core.DRAWS_ON_ATTACKED).ifPresent(draws -> {
                        int owner = data.get(attackTarget, core.OWNED_BY);
                        SystemsUtil.increase(data, owner, core.DRAW_CARDS_REQUEST, draws);
                    });
                }
                data.remove(attacker, core.ATTACKS_TARGET);
            }
        }
        data.set(player, core.START_PHASE_REQUEST, PlayerPhase.ATTACK);
    }

    @Override
    public boolean isActive() {
        return data.list(core.END_PHASE_REQUEST).nonEmpty();
    }

    @Override
    public void modify() {
        for (int entity : data.list(core.END_PHASE_REQUEST)) {
            int phase = data.get(entity, core.END_PHASE_REQUEST);
            //modifiers here
            data.set(entity, core.END_PHASE_ACTION, phase);
            data.remove(entity, core.END_PHASE_REQUEST);
        }
    }

    @Override
    public void apply() {
        for (int player : data.list(core.END_PHASE_ACTION)) {
            data.remove(player, core.ACTIVE_PLAYER_PHASE);
        }
    }

    @Override
    public void triggerAndClean() {
        boolean mulliganEnded = false;
        for (int player : data.list(core.END_PHASE_ACTION)) {
            int phase = data.get(player, core.END_PHASE_ACTION);
            mulliganEnded |= phase == PlayerPhase.MULLIGAN;
            for (Trigger trigger : triggers) {
                trigger.trigger(player, phase);
            }
            data.remove(player, core.END_PHASE_ACTION);
        }

        //mulligan logic
        if (mulliganEnded && data.list(core.ACTIVE_PLAYER_PHASE).isEmpty()) {
            IntMap draws = new IntMap();
            for (int card : data.list(core.MULLIGAN)) {
                int owner = data.get(card, core.OWNED_BY);
                draws.set(owner, draws.getOrElse(owner, 0) + 1);
            }
            for (int player : draws) {
                SystemsUtil.drawCards(data, draws.get(player), random, player);
            }
            for (int card : data.list(core.MULLIGAN)) {
                data.remove(card, core.MULLIGAN);
                data.remove(card, core.IN_HAND_ZONE);
                data.set(card, core.IN_LIBRARY_ZONE, 1);
            }

            Integer startingPlayer = null;
            for (int player : data.list(core.PLAYER_INDEX)) {
                if (data.hasValue(player, core.PLAYER_INDEX, 0)) {
                    startingPlayer = player;
                    break;
                }
            }
            data.set(startingPlayer, core.START_PHASE_REQUEST, PlayerPhase.ATTACK);
        }
    }

}
