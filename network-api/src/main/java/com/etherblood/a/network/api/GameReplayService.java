package com.etherblood.a.network.api;

import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.GameSettingsBuilder;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.etherblood.a.templates.api.TemplatesParser;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.templates.implementation.TemplateAliasMaps;
import java.util.HashMap;
import java.util.Map;

public class GameReplayService {

    private static final String THE_COIN = "the_coin";

    private final GameReplay replay;
    private final Function<String, JsonElement> assetLoader;

    private Game cachedGame = null;

    public GameReplayService(RawGameSetup setup, Function<String, JsonElement> assetLoader) {
        this.assetLoader = assetLoader;
        replay = new GameReplay();
        replay.setup = setup;
        replay.moves = new ArrayList<>();
    }

    public Game createInstance() {
        return createInstance(new NoopGameEventListener());
    }

    public synchronized Game createInstance(GameEventListener listener) {
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        Components components = componentsBuilder.build();

        TemplateAliasMaps templateAliasMaps = new TemplateAliasMaps();
        TemplatesLoader loader = new TemplatesLoader(assetLoader, new TemplatesParser(components, templateAliasMaps.getEffects(), templateAliasMaps.getStatModifiers()));

        Map<String, Integer> cardAliasMap = new HashMap<>();
        RawGameSetup gameData = replay.setup;
        for (RawPlayerSetup player : gameData.players) {
            cardAliasMap.put(player.library.hero, loader.registerCardAlias(player.library.hero));
            for (String card : player.library.cards.keySet()) {
                cardAliasMap.put(card, loader.registerCardAlias(card));
            }
        }
        cardAliasMap.put(THE_COIN, loader.registerCardAlias(THE_COIN));
        GameSettingsBuilder builder = new GameSettingsBuilder();
        builder.components = components;
        builder.templates = loader.buildGameTemplates();
        GameSettings settings = builder.build();
        EntityData data = new SimpleEntityData(settings.components);
        MoveService moves = new MoveService(settings, data, HistoryRandom.producer(), Collections.emptyList(), true, true, listener);
        Game game = new Game(settings, data, moves);
        gameData.toGameSetup(cardAliasMap::get).setup(data, game.getTemplates());
        updateInstance(game);
        return game;
    }

    public synchronized void updateInstance(Game game) {
        for (int i = game.getMoves().getHistory().size(); i < replay.moves.size(); i++) {
            MoveReplay moveReplay = replay.moves.get(i);
            for (int randomResult : moveReplay.randomResults) {
                game.getMoves().getRandom().getHistory().add(randomResult);
            }
            game.getMoves().apply(moveReplay.move);
        }
    }

    public synchronized MoveReplay apply(Move move) {
        updateCachedGame();
        MoveService moves = cachedGame.getMoves();
        moves.apply(move);
        MoveReplay moveReplay = moves.getHistory().get(moves.getHistory().size() - 1);
        assert move == moveReplay.move;
        replay.moves.add(moveReplay);
        return moveReplay;
    }

    public synchronized void apply(MoveReplay moveReplay) {
        replay.moves.add(moveReplay);
    }

    public synchronized boolean isGameOver() {
        updateCachedGame();
        return cachedGame.isGameOver();
    }

    public synchronized int getPlayerIndex(long playerId) {
        RawPlayerSetup[] players = replay.setup.players;
        for (int i = 0; i < players.length; i++) {
            if (players[i].id == playerId) {
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
        return playerByIndex(playerIndex).name;
    }

    private RawPlayerSetup playerByIndex(int playerIndex) {
        return replay.setup.players[playerIndex];
    }
}
