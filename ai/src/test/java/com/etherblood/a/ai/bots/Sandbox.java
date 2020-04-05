package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.ai.bots.mcts.RolloutsToSimpleEvaluation;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.TimeStats;
import com.etherblood.a.rules.setup.SimpleSetup;
import com.etherblood.a.rules.templates.CardCastBuilder;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.CardTemplateBuilder;
import com.etherblood.a.rules.templates.MinionTemplate;
import com.etherblood.a.rules.templates.MinionTemplateBuilder;
import com.etherblood.a.rules.templates.casteffects.SummonEffect;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class Sandbox {

    private final Random random = new SecureRandom();

    @Test
    public void testGame() {
        float[] result = new float[2];
        long[] nanos = new long[2];
        for (int i = 0; i < 1000; i++) {
            Game game = startGame();

            RolloutsToSimpleEvaluation<Move, MoveBotGame> evaluation = new RolloutsToSimpleEvaluation<>(random, 10);
            MctsBotSettings<Move, MoveBotGame> settings0 = new MctsBotSettings<>();
            settings0.strength = 1000;
            settings0.evaluation = evaluation::evaluate;
            MctsBot<Move, MoveBotGame> bot0 = new MctsBot<>(new MoveBotGame(copySettings(game)), settings0);
            MctsBotSettings<Move, MoveBotGame> settings1 = new MctsBotSettings<>();
            settings1.strength = 1000;
            settings1.evaluation = evaluation::evaluate;
            MctsBot<Move, MoveBotGame> bot1 = new MctsBot<>(new MoveBotGame(copySettings(game)), settings1);

            while (!game.isGameOver()) {
                if (game.getActivePlayerIndex() == 0) {
                    long start = System.nanoTime();
                    bot0.playTurn(new MoveBotGame(game));
                    nanos[0] += System.nanoTime() - start;
                } else {
                    long start = System.nanoTime();
                    bot1.playTurn(new MoveBotGame(game));
                    nanos[1] += System.nanoTime() - start;
                }
            }
            if (game.hasPlayerWon(game.findPlayerByIndex(0))) {
                result[0]++;
            } else if (game.hasPlayerWon(game.findPlayerByIndex(1))) {
                result[1]++;
            } else {
                result[0] += 0.5f;
                result[1] += 0.5f;
            }
            System.out.println("Result: " + Arrays.toString(result) + " in " + Arrays.toString(Arrays.stream(nanos).mapToObj(TimeStats::humanReadableNanos).toArray()));
        }
    }

    private Game copySettings(Game game) {
        GameSettings settings = new GameSettings();
        settings.backupsEnabled = false;
        settings.random = random;
        settings.cards = game.getCards();
        settings.minions = game.getMinions();
        return new Game(settings);
    }

    private Game startGame() {
        Map<Integer, CardTemplate> cards = new HashMap<>();
        Map<Integer, MinionTemplate> minions = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            CardTemplateBuilder cardBuilder = new CardTemplateBuilder(cards.size());
            CardCastBuilder castBuilder;
            if (random.nextInt(5) == 0) {
                castBuilder = cardBuilder.getBlockPhaseCast();
            } else {
                castBuilder = cardBuilder.getAttackPhaseCast();
            }
            int manaCost = random.nextInt(4) + random.nextInt(4) + 1;
            int attack = random.nextInt(manaCost + 1) + random.nextInt(manaCost + 1);
            int health = 1 + 2 * manaCost - attack;
            castBuilder.setManaCost(manaCost);
            castBuilder.addEffect(new SummonEffect(i));
            castBuilder.setTargeted(false);
            cards.put(cards.size(), cardBuilder.build());

            MinionTemplateBuilder minionBuilder = new MinionTemplateBuilder(minions.size());
            minionBuilder.set(Components.ATTACK, attack);
            minionBuilder.set(Components.HEALTH, health);
            minions.put(minions.size(), minionBuilder.build());
        }

        MinionTemplateBuilder heroBuilder = new MinionTemplateBuilder(minions.size());
        heroBuilder.set(Components.ATTACK, 0);
        heroBuilder.set(Components.HEALTH, 30);
        heroBuilder.set(Components.MANA_GROWTH, 1);
        minions.put(minions.size(), heroBuilder.build());

        SimpleSetup setup = new SimpleSetup(2);
        IntList library = new IntList();
        for (int i = 0; i < 20; i++) {
            library.add(i % cards.size());
        }
        setup.setLibrary(0, library);
        setup.setLibrary(1, library);
        setup.setHero(0, minions.size() - 1);
        setup.setHero(1, minions.size() - 1);

        GameSettings settings = new GameSettings();
        settings.backupsEnabled = false;
        settings.random = random;
        settings.cards = cards::get;
        settings.minions = minions::get;
        Game game = new Game(settings);
        setup.apply(game);
        game.start();
        return game;
    }
}
