package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.a.ai.bots.evaluation.SimpleEvaluation;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.GameSettingsBuilder;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.setup.SimpleSetup;
import com.etherblood.a.templates.LibraryTemplate;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.etherblood.a.templates.TemplatesLoader;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TestSandbox {

    @Test
    public void testGame() throws InterruptedException {
        float[] result = new float[2];
        long[] nanos = new long[2];
        for (int i = 0; i < 1; i++) {
            Game game = startGame();

            Function<MoveBotGame, float[]> simple = new SimpleEvaluation<Move, MoveBotGame>()::evaluate;

            Function<MoveBotGame, float[]> rolloutEvaluation0 = new RolloutToEvaluation<>(new Random(), 10, simple)::evaluate;
            MctsBotSettings<Move, MoveBotGame> settings0 = new MctsBotSettings<>();
            settings0.strength = 100;
            settings0.evaluation = rolloutEvaluation0;
            MctsBot<Move, MoveBotGame> bot0 = new MctsBot<>(new MoveBotGame(game), new MoveBotGame(simulationGame(game)), settings0);

            Function<MoveBotGame, float[]> rolloutEvaluation1 = new RolloutToEvaluation<>(new Random(), 10, simple)::evaluate;
            MctsBotSettings<Move, MoveBotGame> settings1 = new MctsBotSettings<>();
            settings1.strength = 100;
            settings1.evaluation = rolloutEvaluation1;
            MctsBot<Move, MoveBotGame> bot1 = new MctsBot<>(new MoveBotGame(game), new MoveBotGame(simulationGame(game)), settings1);

            while (!game.isGameOver()) {
                Move move;
                if (game.isPlayerActive(game.findPlayerByIndex(0))) {
                    long start = System.nanoTime();
                    move = bot0.findBestMove(0);
                    nanos[0] += System.nanoTime() - start;

                } else {
                    long start = System.nanoTime();
                    move = bot1.findBestMove(1);
                    nanos[1] += System.nanoTime() - start;
                }
                game.getMoves().move(move);
            }
            if (game.hasPlayerWon(game.findPlayerByIndex(0))) {
                result[0]++;
            } else if (game.hasPlayerWon(game.findPlayerByIndex(1))) {
                result[1]++;
            } else {
                result[0] += 0.5f;
                result[1] += 0.5f;
            }
            System.out.println("Result: " + Arrays.toString(result));
        }
    }

    private Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false);
        return new Game(game.getSettings(), data, moves);
    }

    private Game startGame() {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        Function<String, JsonElement> assetLoader = x -> {
            try {
                return new Gson().fromJson(Files.newBufferedReader(Paths.get("../assets/templates/" + x)), JsonElement.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        TemplatesLoader loader = new TemplatesLoader(assetLoader, new TemplatesParser(settingsBuilder.components));
        
        RawLibraryTemplate rawLibrary = new RawLibraryTemplate();
        rawLibrary.hero = "minions/lots_of_health.json";
        rawLibrary.cards = Arrays.stream(new Gson().fromJson(assetLoader.apply("card_list.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> 1));
        
        LibraryTemplate lib0 = loader.parseLibrary(rawLibrary);
        LibraryTemplate lib1 = lib0;
        settingsBuilder.templates = loader.buildGameTemplates();
        GameSettings settings = settingsBuilder.build();

        IntList library0 = new IntList();
        for (int card : lib0.cards) {
            library0.add(card);
        }
        IntList library1 = new IntList();
        for (int card : lib1.cards) {
            library1.add(card);
        }

        SimpleSetup setup = new SimpleSetup(2);
        setup.setLibrary(0, library0);
        setup.setLibrary(1, library1);
        setup.setHero(0, lib0.hero);
        setup.setHero(1, lib1.hero);

        EntityData data = new SimpleEntityData(settings.components);
        MoveService moves = new MoveService(settings, data, HistoryRandom.producer());
        Game game = new Game(settings, data, moves);
        setup.apply(game);
        moves.move(new Start());
        return game;
    }
}
