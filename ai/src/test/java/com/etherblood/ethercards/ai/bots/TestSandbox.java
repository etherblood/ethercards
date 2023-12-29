package com.etherblood.ethercards.ai.bots;

import com.etherblood.ethercards.ai.MoveBotGame;
import com.etherblood.ethercards.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.ethercards.ai.bots.evaluation.SimpleTeamEvaluation;
import com.etherblood.ethercards.ai.bots.mcts.MctsBot;
import com.etherblood.ethercards.ai.bots.mcts.MctsBotSettings;
import com.etherblood.ethercards.entities.ComponentsBuilder;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.SimpleEntityData;
import com.etherblood.ethercards.game.events.api.NoopGameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.GameSettings;
import com.etherblood.ethercards.rules.GameSettingsBuilder;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.HistoryRandom;
import com.etherblood.ethercards.rules.MoveService;
import com.etherblood.ethercards.rules.classic.GameLoopService;
import com.etherblood.ethercards.rules.moves.Move;
import com.etherblood.ethercards.rules.moves.Start;
import com.etherblood.ethercards.templates.api.RecordTypeAdapterFactory;
import com.etherblood.ethercards.templates.api.TemplatesLoader;
import com.etherblood.ethercards.templates.api.TemplatesParser;
import com.etherblood.ethercards.templates.api.setup.RawGameSetup;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.etherblood.ethercards.templates.api.setup.RawPlayerSetup;
import com.etherblood.ethercards.templates.implementation.TemplateAliasMapsImpl;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class TestSandbox {

    @Test
    public void fullGame_1vs1() throws InterruptedException {
        playGame(1);
    }

    @Test
    public void fullGame_2vs2() throws InterruptedException {
        playGame(2);
    }

    private void playGame(int teamSize) throws InterruptedException {
        int movesPlayed = 0;
        Random random = new Random(7);
        // This test mainly serves as crash detection
        // Note: This is not a benchmark, asserts are enabled
        Game game = startGame(random, teamSize);

        Function<MoveBotGame, float[]> simple = new SimpleTeamEvaluation<Move, MoveBotGame>()::evaluate;

        List<MctsBot> bots = new ArrayList<>();
        for (int i = 0; i < 2 * teamSize; i++) {
            Function<MoveBotGame, float[]> rolloutEvaluation0 = new RolloutToEvaluation<>(random, 10, simple)::evaluate;
            MctsBotSettings<Move, MoveBotGame> settings0 = new MctsBotSettings<>();
            settings0.random = random;
            settings0.strength = 100;
            settings0.evaluation = rolloutEvaluation0;
            MctsBot bot = new MctsBot(new MoveBotGame(game), () -> new MoveBotGame(simulationGame(game, random)), settings0);
            bots.add(bot);
        }
        while (!game.isGameOver()) {
            Move move = null;
            EntityData data = game.getData();
            CoreComponents core = data.getSchema().getModule(CoreComponents.class);
            for (int player : data.listInValueOrder(core.PLAYER_INDEX)) {
                int index = data.get(player, core.PLAYER_INDEX);
                if (game.isPlayerActive(player)) {
                    move = bots.get(index).findMove(index);
                    break;
                }
            }
            game.getMoves().apply(move);
            movesPlayed++;
        }
        System.out.println("Moves played: " + movesPlayed);
    }

    private Game simulationGame(Game game, Random random) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        NoopGameEventListener eventListener = new NoopGameEventListener();
        HistoryRandom producer = HistoryRandom.producer(random::nextInt);
        GameTemplates templates = game.getSettings().templates;
        MoveService moves = new MoveService(data, templates, producer, null, false, false, eventListener, new GameLoopService(data, templates, producer, eventListener));
        return new Game(game.getSettings(), data, moves);
    }

    private Game startGame(Random random, int teamSize) {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        TemplatesLoader loader = new TemplatesLoader(x -> TemplatesLoader.loadFile("../assets/templates/cards/" + x + ".json"), new TemplatesParser(settingsBuilder.components, new TemplateAliasMapsImpl()));

        RawLibraryTemplate rawLibrary = new RawLibraryTemplate(
                "elderwood_ahri",
                Arrays.stream(new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create().fromJson(TemplatesLoader.loadFile("../assets/templates/card_pool.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> 1)));

        List<RawPlayerSetup> players = new ArrayList<>();
        int teamCount = 2;
        for (int team = 0; team < teamCount; team++) {
            for (int i = 0; i < teamSize; i++) {
                RawPlayerSetup player = new RawPlayerSetup(
                        0,
                        null,
                        team,
                        rawLibrary);
                players.add(player);
            }
        }

        RawGameSetup gameSetup = new RawGameSetup(
                teamCount,
                players.toArray(RawPlayerSetup[]::new),
                "the_coin");

        loader.parseLibrary(rawLibrary);
        loader.registerCardAlias(gameSetup.theCoinAlias());

        settingsBuilder.templates = loader.buildGameTemplates();
        GameSettings settings = settingsBuilder.build();

        EntityData data = new SimpleEntityData(settings.components);
        HistoryRandom producer = HistoryRandom.producer(random::nextInt);
        NoopGameEventListener eventListener = new NoopGameEventListener();
        MoveService moves = new MoveService(data, settings.templates, producer, eventListener, new GameLoopService(data, settings.templates, producer, eventListener));
        Game game = new Game(settings, data, moves);
        gameSetup.toGameSetup(loader::registerCardAlias).setup(data, game.getTemplates());
        moves.apply(new Start());
        return game;
    }
}
