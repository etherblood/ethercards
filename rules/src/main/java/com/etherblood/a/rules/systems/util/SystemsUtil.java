package com.etherblood.a.rules.systems.util;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.Components;

import java.util.Random;

public class SystemsUtil {

    public static Integer nextPlayer(EntityData data, int player) {
        int playerIndex = data.get(player, Components.PLAYER_INDEX);
        int bestScore = Integer.MAX_VALUE;
        Integer bestPlayer = null;
        IntList players = data.list(Components.PLAYER_INDEX);
        for (int current : players) {
            if (current == player) {
                continue;
            }
            if (data.has(current, Components.HAS_LOST) || data.has(current, Components.HAS_WON)) {
                continue;
            }
            int currentIndex = data.get(current, Components.PLAYER_INDEX);
            int currentScore = Math.floorMod(currentIndex - playerIndex, players.size());
            if (currentScore < bestScore) {
                bestScore = currentScore;
                bestPlayer = current;
            }
        }
        return bestPlayer;
    }

    public static void increase(EntityData data, int entity, int component, int value) {
        int total = data.getOptional(entity, component).orElse(0);
        total += value;
        data.set(entity, component, total);
    }

    public static void fight(EntityData data, int attacker, int blocker) {
        damage(data, blocker, data.getOptional(attacker, Components.ATTACK).orElse(0));
        damage(data, attacker, data.getOptional(blocker, Components.ATTACK).orElse(0));
        data.remove(attacker, Components.ATTACKS_TARGET);
        data.remove(blocker, Components.BLOCKS_ATTACKER);
    }

    public static void damage(EntityData data, int target, int damage) {
        damage += data.getOptional(target, Components.DAMAGE).orElse(0);
        if (damage != 0) {
            data.set(target, Components.DAMAGE, damage);
        } else {
            data.remove(target, Components.DAMAGE);
        }
    }

    public static void drawCards(EntityData data, int amount, Random random, int player) {
        IntList allLibraryCards = data.list(Components.IN_LIBRARY_ZONE);
        IntList myLibraryCards = new IntList();
        for (int card : allLibraryCards) {
            if (data.hasValue(card, Components.OWNED_BY, player)) {
                myLibraryCards.add(card);
            }
        }

        if (amount >= myLibraryCards.size()) {
            for (int card : myLibraryCards) {
                data.remove(card, Components.IN_LIBRARY_ZONE);
                data.set(card, Components.IN_HAND_ZONE, 1);
            }
            amount -= myLibraryCards.size();
            myLibraryCards.clear();
        } else {
            while (amount > 0) {
                int card = myLibraryCards.swapRemoveAt(random.nextInt(myLibraryCards.size()));
                data.remove(card, Components.IN_LIBRARY_ZONE);
                data.set(card, Components.IN_HAND_ZONE, 1);
                amount--;
            }
        }

        int fatigue = data.getOptional(player, Components.FATIGUE).orElse(0);
        int fatigueDamage = 0;
        for (int i = 0; i < amount; i++) {
            fatigueDamage += ++fatigue;
        }
        applyFatigue(data, player, fatigueDamage);
        data.set(player, Components.FATIGUE, fatigue);
    }

    private static void applyFatigue(EntityData data, int player, int fatigue) {
        IntList minions = data.list(Components.IN_BATTLE_ZONE);
        IntMap effectiveHealth = new IntMap();
        IntList myMinions = new IntList();
        for (int minion : minions) {
            if (data.hasValue(minion, Components.OWNED_BY, player)) {
                int health = data.getOptional(minion, Components.HEALTH).orElse(0);
                int damage = data.getOptional(minion, Components.DAMAGE).orElse(0);
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
