package com.etherblood.a.ai.bots.evaluation;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;

public class SimpleEvaluation<Move, Game extends BotGame<Move, Game>> {

    public float[] evaluate(Game game) {
        if (game.isGameOver()) {
            return game.resultPlayerScores();
        }
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntMap playerMinionCount = new IntMap();
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            if (data.has(minion, core.HERO)) {
                continue;
            }
            int player = data.get(minion, core.OWNER);
            int count = playerMinionCount.getOrElse(player, 0);
            count++;
            playerMinionCount.set(player, count);
        }

        IntMap playerScores = new IntMap();
        for (int player : data.list(core.MANA_POOL)) {
            playerScores.set(player, 10 * data.get(player, core.MANA_POOL));
        }
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            int player = data.get(minion, core.OWNER);
            int score = playerScores.getOrElse(player, 0);
            score += 10 * (data.getOptional(minion, core.ATTACK).orElse(0) + data.getOptional(minion, core.VENOM).orElse(0));
            score += 10 * (data.getOptional(minion, core.HEALTH).orElse(0) - data.getOptional(minion, core.POISONED).orElse(0));
            score += 10 * data.getOptional(minion, core.MANA_POOL_AURA).orElse(0);
            int minionCount = playerMinionCount.getOrElse(player, 0);
            if (minionCount > 1) {
                score += 10 * (minionCount - 1) * (data.getOptional(minion, core.OWN_MINIONS_HEALTH_AURA).orElse(0) + data.getOptional(minion, core.OWN_MINIONS_VENOM_AURA).orElse(0));
            }
            playerScores.set(player, score);
        }
        IntMap handCards = new IntMap();
        for (int card : data.list(core.IN_HAND_ZONE)) {
            int player = data.get(card, core.OWNER);
            int score = handCards.getOrElse(player, 0);
            score += 1;
            handCards.set(player, score);
        }
        IntMap libraryCards = new IntMap();
        for (int card : data.list(core.IN_LIBRARY_ZONE)) {
            int player = data.get(card, core.OWNER);
            int score = libraryCards.getOrElse(player, 0);
            score += 1;
            libraryCards.set(player, score);
        }
        float[] result = new float[game.playerCount()];
        for (int player : data.list(core.PLAYER_INDEX)) {
            float score = playerScores.getOrElse(player, 0);
            score += (float) 15 * handCards.getOrElse(player, 0);
            score += (float) Math.sqrt(libraryCards.getOrElse(player, 0));
            score -= 10 * data.getOptional(player, core.FATIGUE).orElse(0);
            result[data.get(player, core.PLAYER_INDEX)] = Math.max(score, 0.01f);
        }

        float sum = 0;
        for (float f : result) {
            sum += f;
        }
        for (int i = 0; i < result.length; i++) {
            result[i] /= sum;
        }
        return result;
    }
}
