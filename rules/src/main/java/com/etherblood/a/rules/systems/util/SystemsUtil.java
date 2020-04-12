package com.etherblood.a.rules.systems.util;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.MinionTemplate;

import java.util.Random;

public class SystemsUtil {

    public static Integer nextPlayer(EntityData data, int player) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int playerIndex = data.get(player, core.PLAYER_INDEX);
        int bestScore = Integer.MAX_VALUE;
        Integer bestPlayer = null;
        IntList players = data.list(core.PLAYER_INDEX);
        for (int current : players) {
            if (current == player) {
                continue;
            }
            if (data.has(current, core.HAS_LOST) || data.has(current, core.HAS_WON)) {
                continue;
            }
            int currentIndex = data.get(current, core.PLAYER_INDEX);
            int currentScore = Math.floorMod(currentIndex - playerIndex, players.size());
            if (currentScore < bestScore) {
                bestScore = currentScore;
                bestPlayer = current;
            }
        }
        return bestPlayer;
    }

    public static int increase(EntityData data, int entity, int component, int value) {
        int total = data.getOptional(entity, component).orElse(0);
        total += value;
        data.set(entity, component, total);
        return total;
    }

    public static int decreaseAndRemoveLtZero(EntityData data, int entity, int component, int value) {
        int total = data.getOptional(entity, component).orElse(0);
        total -= value;
        if (total <= 0) {
            data.remove(entity, component);
        } else {
            data.set(entity, component, total);
        }
        return total;
    }

    public static void fight(EntityData data, int attacker, int blocker) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        damage(data, blocker, data.getOptional(attacker, core.ATTACK).orElse(0));
        damage(data, attacker, data.getOptional(blocker, core.ATTACK).orElse(0));
        data.remove(attacker, core.ATTACKS_TARGET);
        data.remove(blocker, core.BLOCKS_ATTACKER);
    }

    public static void damage(EntityData data, int target, int damage) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        increase(data, target, core.DAMAGE, damage);
    }

    public static void summon(Game game, int minionTemplate, int owner) {
        EntityData data = game.getData();
        MinionTemplate minion = game.getMinions().apply(minionTemplate);
        int entity = data.createEntity();
        for (int component : minion) {
            data.set(entity, component, minion.get(component));
        }
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.set(entity, core.IN_BATTLE_ZONE, 1);
        data.set(entity, core.OWNED_BY, owner);
    }

    public static void drawCards(EntityData data, int amount, Random random, int player) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList allLibraryCards = data.list(core.IN_LIBRARY_ZONE);
        IntList myLibraryCards = new IntList();
        for (int card : allLibraryCards) {
            if (data.hasValue(card, core.OWNED_BY, player)) {
                myLibraryCards.add(card);
            }
        }

        if (amount >= myLibraryCards.size()) {
            for (int card : myLibraryCards) {
                data.remove(card, core.IN_LIBRARY_ZONE);
                data.set(card, core.IN_HAND_ZONE, 1);
            }
            amount -= myLibraryCards.size();
            myLibraryCards.clear();
        } else {
            while (amount > 0) {
                int card = myLibraryCards.swapRemoveAt(random.nextInt(myLibraryCards.size()));
                data.remove(card, core.IN_LIBRARY_ZONE);
                data.set(card, core.IN_HAND_ZONE, 1);
                amount--;
            }
        }

        int fatigue = data.getOptional(player, core.FATIGUE).orElse(0);
        int fatigueDamage = 0;
        for (int i = 0; i < amount; i++) {
            fatigueDamage += ++fatigue;
        }
        applyFatigue(data, player, fatigueDamage);
        data.set(player, core.FATIGUE, fatigue);
    }

    private static void applyFatigue(EntityData data, int player, int fatigue) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList minions = data.list(core.IN_BATTLE_ZONE);
        IntMap effectiveHealth = new IntMap();
        IntList myMinions = new IntList();
        for (int minion : minions) {
            if (data.hasValue(minion, core.OWNED_BY, player)) {
                int health = data.getOptional(minion, core.HEALTH).orElse(0);
                int damage = data.getOptional(minion, core.DAMAGE).orElse(0);
                effectiveHealth.set(minion, health - damage);
                myMinions.add(minion);
            }
        }
        if (myMinions.isEmpty()) {
            return;
        }
        IntMap fatigueDamage = new IntMap();
        for (int i = 0; i < fatigue; i++) {
            int bestEntity = myMinions.get(0);
            int bestValue = effectiveHealth.get(bestEntity);
            for (int j = 1; j < myMinions.size(); j++) {
                int entity = myMinions.get(j);
                int value = effectiveHealth.get(entity);
                if (value > bestValue) {
                    bestEntity = entity;
                    bestValue = value;
                }
            }

            int prev = fatigueDamage.getOrElse(bestEntity, 0);
            fatigueDamage.set(bestEntity, prev + 1);

            effectiveHealth.set(bestEntity, bestValue - 1);
        }

        for (int minion : fatigueDamage) {
            int fatigueDmg = fatigueDamage.get(minion);
            if (fatigueDmg > 0) {
                SystemsUtil.damage(data, minion, fatigueDmg);
            }
        }
    }

}
