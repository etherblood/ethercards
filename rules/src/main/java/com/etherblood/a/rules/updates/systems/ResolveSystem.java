package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DamageEvent;
import com.etherblood.a.game.events.api.events.DeathEvent;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.EffectiveStatsService;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.TriggerService;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.function.IntUnaryOperator;

public class ResolveSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final TriggerService triggerService;
    private final ZoneService zoneService;

    public ResolveSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
        this.triggerService = new TriggerService(data, templates, random, events);
        this.zoneService = new ZoneService(data, templates, random, events);
    }

    public void run() {
        StateDrivenUpdatesService state = new StateDrivenUpdatesService(data, templates, random, new EffectiveStatsService(data, templates));
        do {
            do {
                discard();
                draw();
                damage();
                death();
                recall();
                playerResults();

                stateUpdates(state);
            } while (data.list(core.DAMAGE_REQUEST).nonEmpty()
                    || data.list(core.DEATH_REQUEST).nonEmpty()
                    || data.list(core.RECALL_REQUEST).nonEmpty()
                    || data.list(core.DRAW_CARDS_REQUEST).nonEmpty()
                    || data.list(core.PLAYER_DISCARD_CARDS).nonEmpty()
                    || data.list(core.DISCARD).nonEmpty()
                    || data.list(core.PLAYER_RESULT_REQUEST).nonEmpty());
        } while (survive());
    }

    private void stateUpdates(StateDrivenUpdatesService state) {
        state.killHealthless();
        state.unbindFreedMinions();
        state.removeInvalidAttacks();
        state.removeInvalidBlocks();
        state.attackWithRagers();
    }

    private void discard() {
        for (int player : data.list(core.PLAYER_DISCARD_CARDS)) {
            int cards = data.get(player, core.PLAYER_DISCARD_CARDS);
            IntList handCards = data.list(core.IN_HAND_ZONE);
            PrimitiveIterator.OfInt iterator = handCards.iterator();
            while (iterator.hasNext()) {
                int card = iterator.nextInt();
                if (!data.hasValue(card, core.OWNER, player)) {
                    iterator.remove();
                }
            }
            for (int i = 0; i < cards && handCards.nonEmpty(); i++) {
                int index = random.applyAsInt(handCards.size());
                int card = handCards.swapRemoveAt(index);
                data.set(card, core.DISCARD, 1);
            }
        }
        data.clear(core.PLAYER_DISCARD_CARDS);
        for (int card : data.list(core.DISCARD)) {
            if (data.has(card, core.IN_HAND_ZONE)) {
                zoneService.removeFromHand(card);
                zoneService.addToGraveyard(card);
            }
        }
        data.clear(core.DISCARD);
    }

    private void draw() {
        for (int player : data.list(core.DRAW_CARDS_REQUEST)) {
            assert data.has(player, core.PLAYER_INDEX);
            int cards = data.get(player, core.DRAW_CARDS_REQUEST);
            if (cards > 0) {
                data.set(player, core.DRAW_CARDS_ACTION, cards);
            }
        }
        data.clear(core.DRAW_CARDS_REQUEST);
        for (int player : data.list(core.DRAW_CARDS_ACTION)) {
            int cards = data.get(player, core.DRAW_CARDS_ACTION);

            for (int i = 0; i < cards; i++) {
                triggerService.onDraw(player);
            }
        }
        for (int player : data.list(core.DRAW_CARDS_ACTION)) {
            int cards = data.get(player, core.DRAW_CARDS_ACTION);
            SystemsUtil.drawCards(data, templates, random, events, player, cards);
        }
        data.clear(core.DRAW_CARDS_ACTION);
    }

    private void damage() {
        for (int entity : data.list(core.DAMAGE_REQUEST)) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                int damage = data.get(entity, core.DAMAGE_REQUEST);
                if (damage > 0) {
                    data.set(entity, core.DAMAGE_ACTION, damage);
                }
            }
        }
        data.clear(core.DAMAGE_REQUEST);

        IntList damaged = data.list(core.DAMAGE_ACTION);
        for (int entity : damaged) {
            int damage = data.get(entity, core.DAMAGE_ACTION);

            if (data.has(entity, core.TRIGGER_SELF_SURVIVE)) {
                data.set(entity, core.DAMAGE_SURVIVAL_REQUEST, damage);
            }
            events.fire(new DamageEvent(entity, damage));
        }
        for (int entity : damaged) {
            int damage = data.get(entity, core.DAMAGE_ACTION);
            assert data.has(entity, core.IN_BATTLE_ZONE);

            if (data.has(entity, core.TEMPORARY_HEALTH)) {
                int temporaryHealth = data.get(entity, core.TEMPORARY_HEALTH);
                if (damage > temporaryHealth) {
                    damage -= temporaryHealth;
                    data.remove(entity, core.TEMPORARY_HEALTH);
                } else {
                    data.set(entity, core.TEMPORARY_HEALTH, temporaryHealth - damage);
                    damage = 0;
                }
            }

            int health = data.getOptional(entity, core.HEALTH).orElse(0);
            data.set(entity, core.HEALTH, health - damage);
        }
        data.clear(core.DAMAGE_ACTION);
    }

    private void death() {
        IntList deathRequests = data.listOrdered(core.DEATH_REQUEST, core.IN_BATTLE_ZONE);
        for (int entity : deathRequests) {
            int deathOptions = data.get(entity, core.DEATH_REQUEST);
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                if (deathOptions != DeathOptions.SACRIFICE) {
                    if (data.has(entity, core.INDESTRUCTIBLE)) {
                        continue;
                    }
                    if (data.has(entity, core.REGENERATION)) {
                        int regeneration = data.get(entity, core.REGENERATION);
                        int owner = data.get(entity, core.OWNER);
                        int mana = data.getOptional(owner, core.MANA).orElse(0);
                        if (regeneration <= mana) {
                            data.set(owner, core.MANA, mana - regeneration);
                            regenerate(entity);
                            continue;
                        }
                    }
                }
                data.set(entity, core.DEATH_ACTION, deathOptions);
            }
        }
        data.clear(core.DEATH_REQUEST);
        IntList deaths = data.list(core.DEATH_ACTION);
        for (int entity : deaths) {
            if (data.has(entity, core.HERO)) {
                int owner = data.get(entity, core.OWNER);
                data.set(owner, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
            }

            triggerService.onDeath(entity);
            events.fire(new DeathEvent(entity));
        }

        for (int entity : deaths) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                zoneService.removeFromBattle(entity);
                zoneService.addToGraveyard(entity);
            }
        }
        data.clear(core.DEATH_ACTION);
    }

    private void recall() {
        for (int entity : data.list(core.RECALL_REQUEST)) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                zoneService.removeFromBattle(entity);
                zoneService.addToHand(entity);
            }
        }
        data.clear(core.RECALL_REQUEST);
    }

    private void regenerate(int entity) {
        CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
        data.set(entity, core.HEALTH, template.getBattle().getComponents().get(core.HEALTH));
        data.set(entity, core.TIRED, 1);
        data.remove(entity, core.ATTACK_TARGET);
        data.remove(entity, core.BLOCK_TARGET);
        for (int other : data.list(core.ATTACK_TARGET)) {
            if (data.hasValue(other, core.ATTACK_TARGET, entity)) {
                data.remove(other, core.ATTACK_TARGET);
            }
        }
        for (int other : data.list(core.BLOCK_TARGET)) {
            if (data.hasValue(other, core.BLOCK_TARGET, entity)) {
                data.remove(other, core.BLOCK_TARGET);
            }
        }
    }

    private void playerResults() {
        IntList playerResultRequests = data.list(core.PLAYER_RESULT_REQUEST);
        for (int player : playerResultRequests) {
            int result = data.get(player, core.PLAYER_RESULT_REQUEST);
            data.set(player, core.PLAYER_RESULT, result);

        }
        data.clear(core.PLAYER_RESULT_REQUEST);
        if (playerResultRequests.nonEmpty()) {
            IntList teams = data.list(core.TEAM_INDEX);
            for (int team : teams) {
                updateTeamResult(team);
            }
            int total = teams.size();
            int losses = 0;
            int wins = 0;
            for (int team : teams) {
                if (data.hasValue(team, core.TEAM_RESULT, PlayerResult.LOSS)) {
                    losses++;
                }
                if (data.hasValue(team, core.TEAM_RESULT, PlayerResult.WIN)) {
                    wins++;
                }
            }
            if (wins != 0) {
                for (int team : teams) {
                    if (!data.has(team, core.TEAM_RESULT)) {
                        setTeamResult(team, PlayerResult.LOSS);
                        break;
                    }
                }
            } else if (losses + 1 == total) {
                for (int team : teams) {
                    if (!data.has(team, core.TEAM_RESULT)) {
                        setTeamResult(team, PlayerResult.WIN);
                        break;
                    }
                }
            }

            for (int player : data.list(core.PLAYER_RESULT)) {
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
            }
        }
    }

    private void setTeamResult(int team, int result) {
        data.set(team, core.TEAM_RESULT, result);
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.TEAM, team)) {
                data.set(player, core.PLAYER_RESULT, result);
            }
        }
    }

    private void updateTeamResult(int team) {
        boolean allLost = true;
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.TEAM, team)) {
                OptionalInt playerResult = data.getOptional(player, core.PLAYER_RESULT);
                if (playerResult.isPresent()) {
                    if (playerResult.getAsInt() == PlayerResult.WIN) {
                        setTeamResult(team, PlayerResult.WIN);
                        return;
                    }
                } else {
                    allLost = false;
                }
            }
        }
        if (allLost) {
            setTeamResult(team, PlayerResult.LOSS);
        }
    }

    private boolean survive() {
        boolean result = false;
        for (int entity : data.list(core.DAMAGE_SURVIVAL_REQUEST)) {
            int survival = data.get(entity, core.DAMAGE_SURVIVAL_REQUEST);
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                data.set(entity, core.DAMAGE_SURVIVAL_ACTION, survival);
                result = true;
            }
        }
        data.clear(core.DAMAGE_SURVIVAL_REQUEST);
        for (int entity : data.list(core.DAMAGE_SURVIVAL_ACTION)) {
            triggerService.onSurvive(entity);
        }
        data.clear(core.DAMAGE_SURVIVAL_ACTION);
        return result;
    }
}
