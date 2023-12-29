package com.etherblood.ethercards.rules.classic;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.EntityList;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.PlayerPhase;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import com.etherblood.ethercards.rules.updates.TriggerService;
import com.etherblood.ethercards.rules.updates.ZoneService;

import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

public class PhaseSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final Runnable resolveSystem;
    private final TriggerService triggerService;
    private final ZoneService zoneService;

    public PhaseSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, Runnable resolveSystem) {
        this.data = data;
        this.core = data.getSchema().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
        this.resolveSystem = resolveSystem;
        this.triggerService = new TriggerService(data, templates, random, events);
        this.zoneService = new ZoneService(data, templates, random, events);
    }

    public void run() {
        while (data.list(core.ACTIVE_PLAYER_PHASE).isEmpty()) {
            EntityList activeTeams = data.list(core.ACTIVE_TEAM_PHASE);
            if (activeTeams.isEmpty()) {
                // game over
                return;
            }
            for (int team : activeTeams) {
                if (data.has(team, core.TEAM_RESULT)) {
                    data.remove(team, core.ACTIVE_TEAM_PHASE);
                    Integer nextTeam = SystemsUtil.nextTeam(data, team);
                    if (nextTeam != null) {
                        startPhase(nextTeam, PlayerPhase.BLOCK);
                    }
                    continue;
                }
                int phase = data.get(team, core.ACTIVE_TEAM_PHASE);
                switch (phase) {
                    case PlayerPhase.ATTACK: {
                        data.remove(team, core.ACTIVE_TEAM_PHASE);
                        endAttackPhase(team);
                        Integer nextTeam = SystemsUtil.nextTeam(data, team);
                        if (nextTeam != null) {
                            startPhase(nextTeam, PlayerPhase.BLOCK);
                        }
                        break;
                    }
                    case PlayerPhase.BLOCK: {
                        endBlockPhase(team);
                        startPhase(team, PlayerPhase.ATTACK);
                        break;
                    }
                    case PlayerPhase.MULLIGAN: {
                        endMulliganPhase();
                        if (data.hasValue(team, core.TEAM_INDEX, 0)) {
                            startPhase(team, PlayerPhase.ATTACK);
                        } else {
                            data.remove(team, core.ACTIVE_TEAM_PHASE);
                        }
                        break;
                    }
                    default: {
                        throw new AssertionError(phase);
                    }
                }
            }
        }
    }

    private void endBlockPhase(int team) {
        applyBlocks(team);
        applyNinjutsu(team);
        applyAttacks(team);
        clearTemporaryStats();
        resolveSystem.run();
    }

    private void applyBlocks(int team) {
        IntList bushidoApplied = new IntList();
        for (int blocker : data.list(core.BLOCK_TARGET)) {
            int blockerTeam = data.get(blocker, core.TEAM);
            if (blockerTeam != team) {
                continue;
            }
            int attacker = data.get(blocker, core.BLOCK_TARGET);
            data.remove(blocker, core.BLOCK_TARGET);
            if (!data.has(attacker, core.IN_BATTLE_ZONE)) {
                continue;
            }
            data.set(attacker, core.BLOCKED, 1);
            if (!bushidoApplied.contains(attacker)) {
                applyBushido(attacker);
                bushidoApplied.add(attacker);
            }
            applyBushido(blocker);
            SystemsUtil.fight(data, templates, random, attacker, blocker, events);
            if (!data.has(attacker, core.TRAMPLE)) {
                data.remove(attacker, core.ATTACK_TARGET);
            }
            SystemsUtil.increase(data, blocker, core.TIRED, 1);
        }
        resolveSystem.run();
    }

    private void applyNinjutsu(int team) {
        for (int ninja : data.listInValueOrder(core.NINJUTSU_ORDER)) {
            int attacker = data.get(ninja, core.NINJUTSU_TARGET);
            if (data.has(attacker, core.ATTACK_TARGET)) {
                int attackTarget = data.get(attacker, core.ATTACK_TARGET);
                if (!data.hasValue(attackTarget, core.TEAM, team)) {
                    continue;
                }
                int owner = data.get(ninja, core.OWNER);
                int mana = data.getOptional(owner, core.MANA).orElse(0);
                int manaCost = data.get(ninja, core.NINJUTSU);
                if (mana >= manaCost && !data.has(attacker, core.BLOCKED)) {
                    SystemsUtil.decreaseAndRemoveLtZero(data, owner, core.MANA, manaCost);

                    zoneService.removeFromBattle(attacker);
                    zoneService.addToHand(attacker);

                    zoneService.removeFromHand(ninja);
                    zoneService.addToBattle(ninja, true);

                    data.set(ninja, core.ATTACK_TARGET, attackTarget);
                    data.set(ninja, core.TIRED, 1);
                    data.set(ninja, core.SUMMONING_SICKNESS, 1);
                }
            }
            data.remove(ninja, core.NINJUTSU_ORDER);
            data.remove(ninja, core.NINJUTSU_TARGET);
        }
    }

    private void applyBushido(int minion) {
        if (data.has(minion, core.BUSHIDO)) {
            int bushido = data.get(minion, core.BUSHIDO);
            SystemsUtil.increase(data, minion, core.TEMPORARY_ATTACK, bushido);
            SystemsUtil.increase(data, minion, core.TEMPORARY_HEALTH, bushido);
        }
    }

    private void applyAttacks(int team) {
        IntList attackers = new IntList();
        for (int attacker : data.list(core.ATTACK_TARGET)) {
            if (!data.has(attacker, core.IN_BATTLE_ZONE)) {
                data.remove(attacker, core.ATTACK_TARGET);
                continue;
            }
            int attackTarget = data.get(attacker, core.ATTACK_TARGET);
            if (data.hasValue(attackTarget, core.TEAM, team)) {
                if (data.has(attackTarget, core.IN_BATTLE_ZONE)) {
                    SystemsUtil.fight(data, templates, random, attacker, attackTarget, events);
                }
                attackers.add(attacker);
            }
        }
        for (int attacker : attackers) {
            data.remove(attacker, core.ATTACK_TARGET);
        }
        resolveSystem.run();
    }

    private void endAttackPhase(int team) {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        for (int attacker : data.list(core.ATTACK_TARGET)) {
            if (!data.hasValue(attacker, core.TEAM, team)) {
                continue;
            }

            if (!stats.hasVigilance(attacker)) {
                data.set(attacker, core.TIRED, 1);
            }
            int target = data.get(attacker, core.ATTACK_TARGET);
            if (!data.has(target, core.IN_BATTLE_ZONE)) {
                data.remove(attacker, core.ATTACK_TARGET);
                continue;
            }

            data.set(target, core.TIRED, 1);
        }
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.hasValue(minion, core.TEAM, team)) {
                SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.SUMMONING_SICKNESS, 1);
            }
        }
        resolveSystem.run();
    }

    private void endMulliganPhase() {
        IntMap draws = new IntMap();
        for (int card : data.list(core.MULLIGAN)) {
            int owner = data.get(card, core.OWNER);
            draws.set(owner, draws.getOrElse(owner, 0) + 1);
        }
        for (int player : draws) {
            SystemsUtil.drawCards(data, templates, random, events, player, draws.get(player));
        }
        for (int card : data.list(core.MULLIGAN)) {
            zoneService.removeFromHand(card);
            zoneService.addToLibrary(card);
        }
        data.clear(core.MULLIGAN);

        // mulligan components are no longer needed, clean them up
        data.clear(core.CANNOT_BE_MULLIGANED);
        resolveSystem.run();
    }

    private void startPhase(int team, int phase) {
        if (data.has(team, core.TEAM_RESULT)) {
            Integer nextTeam = SystemsUtil.nextTeam(data, team);
            if (nextTeam != null) {
                startPhase(nextTeam, PlayerPhase.BLOCK);
            }
            return;
        }
        data.set(team, core.ACTIVE_TEAM_PHASE, phase);
        boolean playerFound = false;
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.TEAM, team) && !data.has(player, core.PLAYER_RESULT)) {
                playerFound = true;
                data.set(player, core.ACTIVE_PLAYER_PHASE, phase);
            }
        }
        assert playerFound;
        switch (phase) {
            case PlayerPhase.BLOCK: {
                startBlockPhase(team);
                break;
            }
            case PlayerPhase.ATTACK: {
                startAttackPhase(team);
                break;
            }
            case PlayerPhase.MULLIGAN:
            default: {
                throw new AssertionError(phase);
            }
        }
    }

    private void startBlockPhase(int team) {
        // nothing happening here?
        resolveSystem.run();
    }

    private void startAttackPhase(int team) {
        for (int minion : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (!data.hasValue(minion, core.TEAM, team)) {
                continue;
            }

            SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.SUMMONING_SICKNESS, 1);
            SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.TIRED, 1);
            data.getOptional(minion, core.MANA_POOL_AURA_GROWTH).ifPresent(growth -> {
                SystemsUtil.increase(data, minion, core.MANA_POOL_AURA, growth);
            });
            data.getOptional(minion, core.POISONED).ifPresent(poison -> {
                SystemsUtil.damage(data, events, minion, poison);
                SystemsUtil.decreaseAndRemoveLtZero(data, minion, core.POISONED, 1);
            });
            data.getOptional(minion, core.DRAWS_PER_TURN).ifPresent(draws -> {
                int owner = data.get(minion, core.OWNER);
                SystemsUtil.drawCards(data, templates, random, events, owner, draws);
            });
        }
        EntityList activePlayers = data.list(core.ACTIVE_PLAYER_PHASE);
        for (int player : activePlayers) {
            assert data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK);
            assert data.hasValue(player, core.TEAM, team);
            int mana = new EffectiveStatsService(data, templates).manaPool(player);
            OptionalInt delayedMana = data.getOptional(player, core.DELAYED_MANA);
            if (delayedMana.isPresent()) {
                mana += delayedMana.getAsInt();
                data.remove(player, core.DELAYED_MANA);
            }
            data.set(player, core.MANA, mana);
        }
        for (int activePlayer : activePlayers) {
            triggerService.onUpkeep(activePlayer);
        }
        resolveSystem.run();
    }

    private void clearTemporaryStats() {
        data.clear(core.TEMPORARY_ATTACK);
        data.clear(core.TEMPORARY_HEALTH);
        data.clear(core.TEMPORARY_HEXPROOF);
        data.clear(core.TEMPORARY_PREVENT_COMBAT_DAMAGE);
        data.clear(core.BLOCKED);
    }
}
