package com.etherblood.a.gui;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.a.ai.bots.evaluation.SimpleEvaluation;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.game.GameSetup;
import com.etherblood.a.network.api.game.PlayerSetup;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapFont;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class GameApplication extends SimpleApplication {

    private GameReplayService gameReplayService;
    private Game game;
    private final AtomicBoolean botIsComputing = new AtomicBoolean(false);

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator("../assets/", FileLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        initGame();
        stateManager.attach(new GameBoardAppstate(this));
    }

    @Override
    public void simpleUpdate(float tpf) {
        gameReplayService.updateInstance(game);
        applyAI();
    }

    private void initGame() {
        Function<String, JsonObject> assetLoader = x -> assetManager.loadAsset(new AssetKey<>("templates/" + x));
        GameSetup setup = new GameSetup();
        PlayerSetup player0 = new PlayerSetup();
        player0.id = 0;
        player0.library = new Gson().fromJson(assetLoader.apply("libraries/default.json"), RawLibraryTemplate.class);
        PlayerSetup player1 = new PlayerSetup();
        player1.id = 1;
        player1.library = new Gson().fromJson(assetLoader.apply("libraries/default.json"), RawLibraryTemplate.class);
        setup.players = new PlayerSetup[]{player0, player1};
        gameReplayService = new GameReplayService(setup, assetLoader);

        game = gameReplayService.createInstance();
        gameReplayService.apply(new Start());
        gameReplayService.updateInstance(game);
    }

    public void applyMove(Move move) {
        if (botIsComputing.get()) {
            System.out.println("User action discarded, bot is still working: " + move);
            return;
        }
        gameReplayService.apply(move);
    }

    private void applyAI() {
        if (!botIsComputing.compareAndSet(false, true)) {
            return;
        }
        Game game = gameReplayService.createInstance();
        int botPlayerIndex = 1;
        int botPlayer = game.findPlayerByIndex(botPlayerIndex);
        if (game.isPlayerActive(botPlayer)) {
            Thread botThread = new Thread(() -> {
                System.out.println("computing...");
                Function<MoveBotGame, float[]> evaluation = new SimpleEvaluation<Move, MoveBotGame>()::evaluate;
                Function<MoveBotGame, float[]> rolloutEvaluation = new RolloutToEvaluation<>(new Random(), 10, evaluation)::evaluate;
                MctsBotSettings<Move, MoveBotGame> botSettings = new MctsBotSettings<>();
                botSettings.verbose = true;
                botSettings.evaluation = rolloutEvaluation;
                botSettings.strength = 10_000;
                MctsBot<Move, MoveBotGame> bot = new MctsBot<>(new MoveBotGame(game), new MoveBotGame(simulationGame(game)), botSettings);
                Move move = bot.findBestMove(botPlayerIndex);
                game.getMoves().move(move);
                System.out.println("Eval: " + Arrays.toString(evaluation.apply(new MoveBotGame(game))));
                botIsComputing.set(false);
                applyMove(move);
                System.out.println("Bot is done.");
            });
            botThread.start();
        } else {
            botIsComputing.set(false);
        }
    }

    private Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false);
        return new Game(game.getSettings(), data, moves);
    }

    public BitmapFont getGuiFont() {
        return guiFont;
    }

    public Game getGame() {
        return game;
    }
}
