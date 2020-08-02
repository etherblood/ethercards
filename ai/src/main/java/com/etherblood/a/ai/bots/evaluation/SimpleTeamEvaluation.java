package com.etherblood.a.ai.bots.evaluation;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.PlayerResult;

public class SimpleTeamEvaluation<Move, Game extends BotGame<Move, Game>> {

    public float[] evaluate(Game game) {
        if (game.isGameOver()) {
            return game.resultPlayerScores();
        }
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        IntMap teamScores = new IntMap();
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            int player = data.get(minion, core.OWNER);
            if (data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                continue;
            }
            int team = data.get(minion, core.TEAM);
            int score = teamScores.getOrElse(team, 0);
            score++;
            score += Math.max(0, data.getOptional(minion, core.ATTACK).orElse(0));
            score += Math.max(0, data.getOptional(minion, core.HEALTH).orElse(0));
            teamScores.set(team, score);
        }
        for (int card : data.list(core.IN_HAND_ZONE)) {
            int player = data.get(card, core.OWNER);
            if (data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS)) {
                continue;
            }

            int team = data.get(card, core.TEAM);
            int score = teamScores.getOrElse(team, 0);
            score++;
            teamScores.set(team, score);
        }
        float[] result = new float[game.playerCount()];
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
