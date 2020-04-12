package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.CoreComponents;
import java.util.List;
import java.util.Random;

public class RolloutsToSimpleEvaluation<Move, Game extends BotGame<Move, Game>> {

    private final Random random;
    private final int rolloutMoves;

    public RolloutsToSimpleEvaluation(Random random, int rolloutMoves) {
        this.random = random;
        this.rolloutMoves = rolloutMoves;
    }

    public float[] evaluate(Game game) {
        for (int i = 0; i < rolloutMoves; i++) {
            if (game.isGameOver()) {
                return game.resultPlayerScores();
            }
            List<Move> moves = game.generateMoves();
            Move move = moves.get(random.nextInt(moves.size()));
            game.applyMove(move);
        }
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        IntMap playerScores = new IntMap();
        for (int minion : data.list(core.IN_BATTLE_ZONE)) {
            int player = data.get(minion, core.OWNED_BY);
            int score = playerScores.getOrElse(player, 0);
            score += 10 * data.getOptional(minion, core.ATTACK).orElse(0);
            score += 10 * data.getOptional(minion, core.HEALTH).orElse(0);
            score += 10 * data.getOptional(minion, core.MANA_POOL).orElse(0);
            playerScores.set(player, score);
        }
        IntMap handCards = new IntMap();
        for (int card : data.list(core.IN_HAND_ZONE)) {
            int player = data.get(card, core.OWNED_BY);
            int score = handCards.getOrElse(player, 0);
            score += 1;
            handCards.set(player, score);
        }
        IntMap libraryCards = new IntMap();
        for (int card : data.list(core.IN_LIBRARY_ZONE)) {
            int player = data.get(card, core.OWNED_BY);
            int score = libraryCards.getOrElse(player, 0);
            score += 1;
            libraryCards.set(player, score);
        }
        float[] result = new float[game.playerCount()];
        for (int player : data.list(core.PLAYER_INDEX)) {
            float score = playerScores.getOrElse(player, 0);
            score += (float) Math.sqrt(2 * handCards.getOrElse(player, 0));
            score += (float) Math.sqrt(libraryCards.getOrElse(player, 0));
            result[data.get(player, core.PLAYER_INDEX)] = score;
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
