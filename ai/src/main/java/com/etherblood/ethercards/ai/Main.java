package com.etherblood.ethercards.ai;

import com.etherblood.ethercards.ai.bots.Bot;
import com.etherblood.ethercards.ai.bots.mcts.MctsBot;
import com.etherblood.ethercards.ai.bots.mcts.MctsBotSettings;
import com.etherblood.ethercards.ai.library.builder.BattleSetup;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.moves.Move;
import com.etherblood.ethercards.templates.api.RecordTypeAdapterFactory;
import com.etherblood.ethercards.templates.api.TemplatesLoader;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.google.gson.GsonBuilder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10_000);// time for profiler to attach
        String assetsPath = "../assets/";
        BattleSetup setup = new BattleSetup(assetsPath + "templates/", new SecureRandom());
        RawLibraryTemplate botLibrary = new RawLibraryTemplate(
                "elderwood_ahri",
                Arrays.stream(new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create().fromJson(TemplatesLoader.loadFile(assetsPath + "templates/card_pool.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> 1)));

        MctsBotSettings<Move, MoveBotGame> candidateSettings = new MctsBotSettings<>();
        candidateSettings.strength = 100;

        MctsBotSettings<Move, MoveBotGame> currentSettings = new MctsBotSettings<>();
        currentSettings.strength = 100;

        int batchCount = 100;
        int batchSize = 10;

        long nanosA = 0;
        long nanosB = 0;

        int wins = 0;
        int draws = 0;
        int losses = 0;
        for (int i = 0; i < batchCount; i++) {
            for (int j = 0; j < batchSize; j++) {
                Game game = setup.startGame(botLibrary, botLibrary);
                Bot candidate = new MctsBot(new MoveBotGame(game), () -> new MoveBotGame(setup.simulationGame(game)), candidateSettings);
                Bot current = new MctsBot(new MoveBotGame(game), () -> new MoveBotGame(setup.simulationGame(game)), currentSettings);

                while (!game.isGameOver()) {
                    long nanos = System.nanoTime();
                    Bot bot;
                    int index;
                    if (game.isPlayerActive(game.findPlayerByIndex(0))) {
                        bot = candidate;
                        index = 0;
                    } else {
                        bot = current;
                        index = 1;
                    }
                    Move move = bot.findMove(index);
                    game.getMoves().apply(move);
                    if (bot == candidate) {
                        nanosA += System.nanoTime() - nanos;
                    } else {
                        nanosB += System.nanoTime() - nanos;
                    }
                }

                if (game.hasPlayerWon(game.findPlayerByIndex(0))) {
                    wins++;
                } else if (game.hasPlayerWon(game.findPlayerByIndex(1))) {
                    losses++;
                } else {
                    draws++;
                }
            }
            System.out.println();
            System.out.println("Used time: " + (nanosA / 1_000_000_000) + "s vs " + (nanosB / 1_000_000_000) + "s");
            System.out.println(wins + " wins, " + draws + " draws, " + losses + " losses (" + (wins + losses + draws) + " / " + (batchCount * batchSize) + " games)");
            printLikelihoodOfSuperiority(wins, draws, losses);
        }

    }

    // https://www.chessprogramming.org/Match_Statistics#Likelihood_of_Superiority
    private static void printLikelihoodOfSuperiority(int wins, int draws, int losses) {
        double games = wins + losses + draws;
        double winning_fraction = (wins + 0.5 * draws) / games;
        double elo_difference = -Math.log(1.0 / winning_fraction - 1.0) * 400.0 / Math.log(10);
        System.out.println("elo difference: " + elo_difference);
        double los = .5 + .5 * erf((wins - losses) / Math.sqrt(2.0 * (wins + losses)));
        System.out.println("los: " + los);
    }

    // https://introcs.cs.princeton.edu/java/21function/ErrorFunction.java.html
    // fractional error in math formula less than 1.2 * 10 ^ -7.
    // although subject to catastrophic cancellation when z in very close to 0
    // from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
    public static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp(-z * z - 1.26551223
                + t * (1.00002368
                + t * (0.37409196
                + t * (0.09678418
                + t * (-0.18628806
                + t * (0.27886807
                + t * (-1.13520398
                + t * (1.48851587
                + t * (-0.82215223
                + t * (0.17087277))))))))));
        if (z >= 0) {
            return ans;
        } else {
            return -ans;
        }
    }
}
