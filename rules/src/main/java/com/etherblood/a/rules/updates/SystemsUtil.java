package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.BattleEvent;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Effect;

import java.util.function.IntUnaryOperator;

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
            if (data.has(current, core.PLAYER_RESULT)) {
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

    public static void decreaseAndRemoveLtZero(EntityData data, int entity, int component, int value) {
        data.getOptional(entity, component).ifPresent(previous -> setAndRemoveLtZero(data, entity, component, previous - value));
    }

    public static void setAndRemoveLtZero(EntityData data, int entity, int component, int value) {
        if (value <= 0) {
            data.remove(entity, component);
        } else {
            data.set(entity, component, value);
        }
    }

    public static void fight(EntityData data, GameTemplates templates, IntUnaryOperator random, int attacker, int blocker, GameEventListener events) {
        EffectiveStatsService stats = new EffectiveStatsService(data, templates);
        events.fire(new BattleEvent(attacker, blocker));
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int attackerDamage = stats.attack(attacker);
        int blockerDamage = stats.attack(blocker);

        damage(data, blocker, attackerDamage);
        damage(data, attacker, blockerDamage);

        if (data.has(attacker, core.LIFELINK)) {
            int attackerOwner = data.get(attacker, core.OWNER);
            increase(data, randomHero(data, random, attackerOwner), core.HEALTH, attackerDamage);
        }
        if (data.has(blocker, core.LIFELINK)) {
            int blockerOwner = data.get(blocker, core.OWNER);
            increase(data, randomHero(data, random, blockerOwner), core.HEALTH, blockerDamage);
        }

        int attackerVenom = stats.venom(attacker);
        if (attackerVenom != 0) {
            increase(data, blocker, core.POISONED, attackerVenom);
        }
        int blockerVenom = stats.venom(blocker);
        if (blockerVenom != 0) {
            increase(data, attacker, core.POISONED, blockerVenom);
        }

        int attackerTemplateId = data.get(attacker, core.CARD_TEMPLATE);
        CardTemplate attackerTemplate = templates.getCard(attackerTemplateId);
        for (Effect afterBattleEffect : attackerTemplate.getAfterSelfBattleEffects()) {
            //TODO: blocker & attacker should be passed as arguments after improving targeting
            afterBattleEffect.apply(data, templates, random, events, attacker, attacker);
        }

        int blockerTemplateId = data.get(blocker, core.CARD_TEMPLATE);
        CardTemplate blockerTemplate = templates.getCard(blockerTemplateId);
        for (Effect afterBattleEffect : blockerTemplate.getAfterSelfBattleEffects()) {
            //TODO: blocker & attacker should be passed as arguments after improving targeting
            afterBattleEffect.apply(data, templates, random, events, blocker, blocker);
        }
    }

    public static void damage(EntityData data, int target, int damage) {
        if (damage <= 0) {
            return;
        }
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        increase(data, target, core.DAMAGE_REQUEST, damage);
    }

    public static int createCard(EntityData data, int templateId, int owner) {
        int card = data.createEntity();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        data.set(card, core.CARD_TEMPLATE, templateId);
        data.set(card, core.OWNER, owner);
        data.set(card, core.TEAM, data.get(owner, core.TEAM));
        return card;
    }

    public static int createHero(EntityData data, GameTemplates templates, int templateId, int owner) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int hero = createMinion(data, templates, templateId, owner);
        data.set(hero, core.HERO, 1);
        //TODO: mana growth & draws should be somehow added to the player, not their hero
        SystemsUtil.increase(data, hero, core.MANA_POOL_AURA_GROWTH, 1);
        SystemsUtil.increase(data, hero, core.DRAWS_PER_TURN, 1);
        return hero;
    }

    public static int summonMinion(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int minionTemplate, int owner) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int minion = createMinion(data, templates, minionTemplate, owner);
        data.set(minion, core.SUMMONING_SICKNESS, 1);
        for (int other : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (minion == other) {
                continue;
            }
            int otherTemplateId = data.get(other, core.CARD_TEMPLATE);
            CardTemplate otherTemplate = templates.getCard(otherTemplateId);
            for (Effect effect : otherTemplate.getOnSummonEffects()) {
                effect.apply(data, templates, random, events, other, minion);
            }
        }
        return minion;
    }

    public static int createMinion(EntityData data, GameTemplates templates, int templateId, int owner) {
        int minion = createCard(data, templateId, owner);
        new ZoneService(data, templates).addToBattle(minion);
        return minion;
    }

    public static int randomHero(EntityData data, IntUnaryOperator random, int player) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList ownHeroes = new IntList();
        for (int hero : data.list(core.HERO)) {
            if (data.hasValue(hero, core.OWNER, player)) {
                ownHeroes.add(hero);
            }
        }
        return ownHeroes.getRandomItem(random);
    }

    public static void drawCards(EntityData data, int amount, IntUnaryOperator random, int player) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntList allLibraryCards = data.list(core.IN_LIBRARY_ZONE);
        IntList myLibraryCards = new IntList();
        for (int card : allLibraryCards) {
            if (data.hasValue(card, core.OWNER, player)) {
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
                int card = myLibraryCards.swapRemoveAt(random.applyAsInt(myLibraryCards.size()));
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
        IntList ownHeroes = new IntList();
        for (int hero : data.list(core.HERO)) {
            if (data.hasValue(hero, core.OWNER, player) && data.has(hero, core.IN_BATTLE_ZONE)) {
                ownHeroes.add(hero);
            }
        }
        if (ownHeroes.isEmpty()) {
            return;
        }
        if (ownHeroes.size() == 1) {
            SystemsUtil.damage(data, ownHeroes.get(0), fatigue);
            return;
        }
        IntMap effectiveHealth = new IntMap();
        for (int hero : ownHeroes) {
            int health = data.getOptional(hero, core.HEALTH).orElse(0);
            int damage = data.getOptional(hero, core.DAMAGE_REQUEST).orElse(0);
            effectiveHealth.set(hero, health - damage);
        }
        IntMap fatigueDamage = new IntMap();
        for (int i = 0; i < fatigue; i++) {
            int bestEntity = ownHeroes.get(0);
            int bestValue = effectiveHealth.get(bestEntity);
            for (int j = 1; j < ownHeroes.size(); j++) {
                int entity = ownHeroes.get(j);
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
            SystemsUtil.damage(data, minion, fatigueDmg);
        }
    }

}
