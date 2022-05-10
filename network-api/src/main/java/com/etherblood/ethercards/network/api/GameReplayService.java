package com.etherblood.ethercards.network.api;

import com.etherblood.ethercards.entities.Components;
import com.etherblood.ethercards.entities.ComponentsBuilder;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.SimpleEntityData;
import com.etherblood.ethercards.game.events.api.GameEventListener;
import com.etherblood.ethercards.game.events.api.NoopGameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.GameSettings;
import com.etherblood.ethercards.rules.GameSettingsBuilder;
import com.etherblood.ethercards.rules.HistoryRandom;
import com.etherblood.ethercards.rules.MoveReplay;
import com.etherblood.ethercards.rules.MoveService;
import com.etherblood.ethercards.rules.classic.GameLoopService;
import com.etherblood.ethercards.rules.moves.Move;
import com.etherblood.ethercards.templates.api.TemplatesLoader;
import com.etherblood.ethercards.templates.api.TemplatesParser;
import com.etherblood.ethercards.templates.api.setup.RawGameSetup;
import com.etherblood.ethercards.templates.api.setup.RawPlayerSetup;
import com.etherblood.ethercards.templates.implementation.TemplateAliasMapsImpl;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

public class GameReplayService {

    private static final String THE_COIN = "the_coin";

    private final GameReplay replay;
    private final Function<String, JsonElement> assetLoader;

    private Game cachedGame = null;

    public GameReplayService(RawGameSetup setup, Function<String, JsonElement> assetLoader) {
        this(new GameReplay(setup, new ArrayList<>()), assetLoader);
    }

    public GameReplayService(GameReplay replay, Function<String, JsonElement> assetLoader) {
        this.replay = replay;
        this.assetLoader = assetLoader;
    }

    public synchronized GameReplay cloneReplay() {
        return new GameReplay(replay);
    }

    public Game createInstance() {
        return createInstance(new NoopGameEventListener());
    }

    public synchronized Game createInstance(GameEventListener listener) {
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        Components components = componentsBuilder.build();

        TemplatesLoader loader = new TemplatesLoader(assetLoader, new TemplatesParser(components, new TemplateAliasMapsImpl()));

        RawGameSetup gameData = replay.setup();
        for (RawPlayerSetup player : gameData.players()) {
            loader.parseLibrary(player.library());
        }
        loader.registerCardAlias(THE_COIN);
        GameSettingsBuilder builder = new GameSettingsBuilder();
        builder.components = components;
        builder.templates = loader.buildGameTemplates();
        GameSettings settings = builder.build();
        EntityData data = new SimpleEntityData(settings.components);
        HistoryRandom random = HistoryRandom.producer();
        MoveService moves = new MoveService(data, settings.templates, random, Collections.emptyList(), true, true, listener, new GameLoopService(data, settings.templates, random, listener), new EffectiveStatsService(data, settings.templates));
        Game game = new Game(settings, data, moves);
        gameData.toGameSetup(loader::registerCardAlias).setup(data, game.getTemplates());
        updateInstance(game);
        return game;
    }

    public synchronized void updateInstance(Game game) {
        for (int i = game.getMoves().getHistory().size(); i < replay.moves().size(); i++) {
            MoveReplay moveReplay = replay.moves().get(i);
            for (int randomResult : moveReplay.randomResults()) {
                game.getMoves().getRandom().getHistory().add(randomResult);
            }
            game.getMoves().apply(moveReplay.move());
        }
    }

    public synchronized MoveReplay apply(Move move) {
        updateCachedGame();
        MoveService moves = cachedGame.getMoves();
        moves.apply(move);
        MoveReplay moveReplay = moves.getHistory().get(moves.getHistory().size() - 1);
        assert move == moveReplay.move();
        replay.moves().add(moveReplay);
        return moveReplay;
    }

    public synchronized void apply(MoveReplay moveReplay) {
        replay.moves().add(moveReplay);
    }

    public synchronized boolean isGameOver() {
        updateCachedGame();
        return cachedGame.isGameOver();
    }

    public synchronized int getPlayerIndex(long playerId) {
        RawPlayerSetup[] players = replay.setup().players();
        for (int i = 0; i < players.length; i++) {
            if (players[i].id() == playerId) {
                return i;
            }
        }
        throw new NullPointerException();
    }

    public synchronized int getPlayerEntity(int playerIndex) {
        updateCachedGame();
        return cachedGame.findPlayerByIndex(playerIndex);
    }

    public synchronized boolean hasPlayerWon(int player) {
        updateCachedGame();
        return cachedGame.hasPlayerWon(player);
    }

    public synchronized boolean hasPlayerLost(int player) {
        updateCachedGame();
        return cachedGame.hasPlayerLost(player);
    }

    private void updateCachedGame() {
        if (cachedGame == null) {
            cachedGame = createInstance();
        } else {
            updateInstance(cachedGame);
        }
    }

    public synchronized String getPlayerName(int playerIndex) {
        return playerByIndex(playerIndex).name();
    }

    private RawPlayerSetup playerByIndex(int playerIndex) {
        return replay.setup().players()[playerIndex];
    }
}
