package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.a.ai.bots.evaluation.SimpleEvaluation;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.GameSettingsBuilder;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.etherblood.a.templates.api.TemplatesParser;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.etherblood.a.templates.implementation.TemplateAliasMaps;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TestSandbox {

    @Test
    public void simulateGame() throws InterruptedException {
        int movesPlayed = 0;
        Random random = new Random(7);
        // This test mainly serves as crash detection
        // Note: This is not a benchmark, asserts are enabled
        float[] result = new float[2];
        for (int i = 0; i < 1; i++) {
            Game game = startGame(random);

            Function<MoveBotGame, float[]> simple = new SimpleEvaluation<Move, MoveBotGame>()::evaluate;

            Function<MoveBotGame, float[]> rolloutEvaluation0 = new RolloutToEvaluation<>(random, 10, simple)::evaluate;
            MctsBotSettings<Move, MoveBotGame> settings0 = new MctsBotSettings<>();
            settings0.random = random;
            settings0.strength = 100;
            settings0.evaluation = rolloutEvaluation0;
            MctsBot bot0 = new MctsBot(new MoveBotGame(game), () -> new MoveBotGame(simulationGame(game, random)), settings0);

            Function<MoveBotGame, float[]> rolloutEvaluation1 = new RolloutToEvaluation<>(random, 10, simple)::evaluate;
            MctsBotSettings<Move, MoveBotGame> settings1 = new MctsBotSettings<>();
            settings1.random = random;
            settings1.strength = 100;
            settings1.evaluation = rolloutEvaluation1;
            MctsBot bot1 = new MctsBot(new MoveBotGame(game), () -> new MoveBotGame(simulationGame(game, random)), settings1);

            while (!game.isGameOver()) {
                Move move;
                if (game.isPlayerActive(game.findPlayerByIndex(0))) {
                    move = bot0.findMove(0);

                } else {
                    move = bot1.findMove(1);
                }
                game.getMoves().apply(move);
                movesPlayed++;
            }
            if (game.hasPlayerWon(game.findPlayerByIndex(0))) {
                result[0]++;
            } else if (game.hasPlayerWon(game.findPlayerByIndex(1))) {
                result[1]++;
            } else {
                result[0] += 0.5f;
                result[1] += 0.5f;
            }
        }
        System.out.println("Moves played: " + movesPlayed);
    }

    private Game simulationGame(Game game, Random random) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(random::nextInt), null, false, false, new NoopGameEventListener());
        return new Game(game.getSettings(), data, moves);
    }

    private Game startGame(Random random) {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        TemplateAliasMaps templateAliasMaps = new TemplateAliasMaps();
        TemplatesLoader loader = new TemplatesLoader(x -> TemplatesLoader.loadFile("../assets/templates/cards/" + x + ".json"), new TemplatesParser(settingsBuilder.components, templateAliasMaps.getEffects(), templateAliasMaps.getStatModifiers()));

        RawLibraryTemplate rawLibrary = new RawLibraryTemplate();
        rawLibrary.hero = "lots_of_health";
        rawLibrary.cards = Arrays.stream(new Gson().fromJson(TemplatesLoader.loadFile("../assets/templates/card_pool.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> 1));

        RawPlayerSetup playerSetup0 = new RawPlayerSetup();
        playerSetup0.library = rawLibrary;
        playerSetup0.teamIndex = 0;
        RawPlayerSetup playerSetup1 = new RawPlayerSetup();
        playerSetup1.library = rawLibrary;
        playerSetup1.teamIndex = 1;

        RawGameSetup gameSetup = new RawGameSetup();
        gameSetup.players = new RawPlayerSetup[]{playerSetup0, playerSetup1};
        gameSetup.teamCount = 2;
        gameSetup.theCoinAlias = "the_coin";
        
        loader.parseLibrary(rawLibrary);
        loader.registerCardAlias(gameSetup.theCoinAlias);

        settingsBuilder.templates = loader.buildGameTemplates();
        GameSettings settings = settingsBuilder.build();

        

        EntityData data = new SimpleEntityData(settings.components);
        MoveService moves = new MoveService(settings, data, HistoryRandom.producer(random::nextInt), new NoopGameEventListener());
        Game game = new Game(settings, data, moves);
        gameSetup.toGameSetup(loader::registerCardAlias).setup(data, game.getTemplates());
        moves.apply(new Start());
        return game;
    }
}
