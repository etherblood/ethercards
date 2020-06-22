package com.etherblood.a.library.builder.ai;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
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
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.setup.SimpleSetup;
import com.etherblood.a.templates.LibraryTemplate;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.etherblood.a.templates.TemplatesLoader;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.JsonElement;
import java.util.function.Function;

public class BattleSetup {

    private final Function<String, JsonElement> assetLoader;
    private final MctsBotSettings<Move, MoveBotGame> botSettings;

    public BattleSetup(Function<String, JsonElement> assetLoader, int strength) {
        this.assetLoader = assetLoader;
        botSettings = new MctsBotSettings<>();
        botSettings.strength = strength;
    }

    public int battle(RawLibraryTemplate a, RawLibraryTemplate b) throws InterruptedException {
        Game activeGame = startGame(a, b);

        MctsBot<Move, MoveBotGame> bot = new MctsBot<>(new MoveBotGame(activeGame), new MoveBotGame(simulationGame(activeGame)), botSettings);

        while (!activeGame.isGameOver()) {
            for (int i = 0; i < 2; i++) {
                int player = activeGame.findPlayerByIndex(i);
                if (activeGame.isPlayerActive(player)) {
                    Move move = bot.findBestMove(i);
                    activeGame.getMoves().apply(move);
                    break;
                }
            }
        }
        if (activeGame.hasPlayerWon(activeGame.findPlayerByIndex(0))) {
            return 1;
        }
        if (activeGame.hasPlayerWon(activeGame.findPlayerByIndex(1))) {
            return -1;
        }
        return 0;
    }

    private Game startGame(RawLibraryTemplate a, RawLibraryTemplate b) {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();

        TemplatesLoader loader = new TemplatesLoader(assetLoader, new TemplatesParser(settingsBuilder.components));

        LibraryTemplate lib0 = loader.parseLibrary(a);
        LibraryTemplate lib1 = loader.parseLibrary(b);
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
        MoveService moves = new MoveService(settings, data, HistoryRandom.producer(), new NoopGameEventListener());
        Game game = new Game(settings, data, moves);
        setup.apply(game);
        moves.apply(new Start());
        return game;
    }

    private Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false, new NoopGameEventListener());
        return new Game(game.getSettings(), data, moves);
    }
}
