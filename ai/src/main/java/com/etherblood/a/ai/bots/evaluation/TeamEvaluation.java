package com.etherblood.a.ai.bots.evaluation;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerResult;

public class TeamEvaluation<Move, Game extends BotGame<Move, Game>> {

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

        IntMap teamScores = new IntMap();
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            int player = data.get(minion, core.OWNER);
            if (data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                continue;
            }
            int team = data.get(minion, core.TEAM);
            int score = teamScores.getOrElse(team, 0);
            score += 10 * (data.getOptional(minion, core.ATTACK).orElse(0) + data.getOptional(minion, core.VENOM).orElse(0));
            score += 10 * (data.getOptional(minion, core.HEALTH).orElse(0) - data.getOptional(minion, core.POISONED).orElse(0));
            score += 10 * data.getOptional(minion, core.MANA_POOL_AURA).orElse(0);
            int minionCount = playerMinionCount.getOrElse(player, 0);
            if (minionCount > 1) {
                score += 10 * (minionCount - 1) * (data.getOptional(minion, core.OWN_MINIONS_HEALTH_AURA).orElse(0) + data.getOptional(minion, core.OWN_MINIONS_VENOM_AURA).orElse(0));
            }
            teamScores.set(team, score);
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
            if (data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                continue;
            }
            int team = data.get(player, core.TEAM);
            int score = teamScores.getOrElse(team, 0);
            score += 15 * handCards.getOrElse(player, 0);
            score += Math.sqrt(libraryCards.getOrElse(player, 0));
            score -= 10 * data.getOptional(player, core.FATIGUE).orElse(0);
            teamScores.set(team, Math.max(score, 0));
        }

        float teamScoreSum = 1;
        for (int team : teamScores) {
            teamScoreSum += teamScores.getOrElse(team, 0);
        }

        for (int player : data.list(core.PLAYER_INDEX)) {
            int team = data.get(player, core.TEAM);
            int index = data.get(player, core.PLAYER_INDEX);
            result[index] = teamScores.getOrElse(team, 0) / teamScoreSum;
        }
        return result;
    }
}
