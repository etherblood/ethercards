package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.effects.Effect;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class StartPhaseSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final Trigger[] triggers;

    public StartPhaseSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
        triggers = new Trigger[]{
            (player, phase) -> {
                switch (phase) {
                    case PlayerPhase.BLOCK: {
                        // do nothing
                        break;
                    }
                    case PlayerPhase.ATTACK: {
                        startAttackPhase(player);
                        break;
                    }
                    case PlayerPhase.MULLIGAN: {
                        // do nothing
                        break;
                    }
                    case PlayerPhase.BATTLE: {
                        startBattlePhase(player);
                        break;
                    }
                }
            }};
    }

    private void startAttackPhase(int player) {
        int mana = 0;
        int draws = data.getOptional(player, core.DRAW_CARDS_REQUEST).orElse(0);
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (!data.hasValue(minion, core.OWNED_BY, player)) {
                continue;
            }

            SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.SUMMONING_SICKNESS, 1);
            SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.TIRED, 1);
            data.getOptional(minion, core.MANA_GROWTH).ifPresent(growth -> {
                SystemsUtil.increase(data, minion, core.MANA_POOL, growth);
            });
            data.getOptional(minion, core.POISONED).ifPresent(poison -> {
                SystemsUtil.damage(data, minion, poison);
                SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.POISONED, 1);
            });
            mana += data.getOptional(minion, core.MANA_POOL).orElse(0);
            draws += data.getOptional(minion, core.DRAWS_PER_TURN).orElse(0);
        }
        mana = Math.max(mana, 0);
        data.set(player, core.MANA, mana);

        draws = Math.max(draws, 0);
        data.set(player, core.DRAW_CARDS_REQUEST, draws);
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (!data.hasValue(minion, core.OWNED_BY, player)) {
                continue;
            }

            int templateId = data.get(minion, core.MINION_TEMPLATE);
            MinionTemplate template = templates.getMinion(templateId);
            for (Effect onUpkeepEffect : template.getOnUpkeepEffects()) {
                onUpkeepEffect.apply(data, templates, random, events, minion, ~0);
            }
        }
    }

    private void startBattlePhase(int player) {
        for (int blocker : data.list(core.BLOCKS_ATTACKER)) {
            int owner = data.get(blocker, core.OWNED_BY);
            if (owner != player) {
                continue;
            }
            int attacker = data.get(blocker, core.BLOCKS_ATTACKER);
            data.remove(blocker, core.BLOCKS_ATTACKER);
            if (!data.has(attacker, core.IN_BATTLE_ZONE)) {
                continue;
            }
            SystemsUtil.fight(data, templates, random, attacker, blocker, events);
            if (!data.has(attacker, core.TRAMPLE)) {
                data.remove(attacker, core.ATTACKS_TARGET);
            }
            SystemsUtil.increase(data, blocker, core.TIRED, 1);

            data.getOptional(blocker, core.DRAWS_ON_BLOCK).ifPresent(draws -> {
                SystemsUtil.increase(data, owner, core.DRAW_CARDS_REQUEST, draws);
            });
        }
        data.set(player, core.END_PHASE_REQUEST, PlayerPhase.BATTLE);
    }

    @Override
    public boolean isActive() {
        return data.list(core.START_PHASE_REQUEST).nonEmpty();
    }

    @Override
    public void modify() {
        for (int entity : data.list(core.START_PHASE_REQUEST)) {
            int phase = data.get(entity, core.START_PHASE_REQUEST);
            data.remove(entity, core.START_PHASE_REQUEST);

            //modifiers here
            if (data.has(entity, core.PLAYER_RESULT)) {
                continue;
            }

            data.set(entity, core.START_PHASE_ACTION, phase);
        }
    }

    @Override
    public void apply() {
        for (int player : data.list(core.START_PHASE_ACTION)) {
            int phase = data.get(player, core.START_PHASE_ACTION);
            data.set(player, core.ACTIVE_PLAYER_PHASE, phase);
        }
    }

    @Override
    public void triggerAndClean() {
        for (int player : data.list(core.START_PHASE_ACTION)) {
            int phase = data.get(player, core.START_PHASE_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(player, phase);
            }
            data.remove(player, core.START_PHASE_ACTION);
        }
    }

}
