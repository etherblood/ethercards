package com.etherblood.a.gui;

import com.etherblood.a.client.GameClient;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.gui.soprettyboard.PostFilterAppstate;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.etherblood.a.templates.TemplatesLoader;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameApplication extends SimpleApplication {

    private final JwtAuthentication authentication;
    private final String assetsPath;
    private final String hostUrl;
    private CardImages cardImages;
    private MyDeckBuilderAppstate deckBuilderAppstate;
    private Future<GameReplayService> futureGameReplayService;
    private GameClient client;

    private Integer selectedStrength;
    private RawLibraryTemplate selectedLibrary;

    public GameApplication(Properties properties, JwtAuthentication authentication) throws Exception {
        this.authentication = authentication;
        this.assetsPath = properties.getProperty("assets");
        this.hostUrl = properties.getProperty("hostUrl");
    }

    private void requestGame() {
        Function<String, JsonElement> assetLoader = x -> load("templates/" + x, JsonElement.class);

        client = new GameClient(assetLoader);
        try {
            client.start(hostUrl);
            if (selectedStrength == 0) {
                futureGameReplayService = client.requestGame(authentication.rawJwt, selectedLibrary);
                stateManager.getState(HudTextAppstate.class).setText("Waiting for opponent...");
            } else if (selectedStrength > 0) {
                futureGameReplayService = client.requestBotgame(authentication.rawJwt, selectedLibrary, selectedStrength * 10_000);
            } else {
                throw new IllegalArgumentException(Integer.toString(selectedStrength));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void startDeckbuilder() {
        stateManager.getState(HudTextAppstate.class).setText("Create your library.");
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);

        TemplatesLoader templatesLoader = new TemplatesLoader(x -> load("templates/" + x, JsonElement.class), new TemplatesParser(componentsBuilder.build()));
        String[] cardAliases = load("templates/card_pool.json", String[].class);
        Map<String, Integer> cardAliasToId = new LinkedHashMap<>();
        for (String card : cardAliases) {
            int id = templatesLoader.registerCardAlias(card);
            cardAliasToId.put(card, id);
        }
        GameTemplates templates = templatesLoader.buildGameTemplates();
        List<DisplayCardTemplate> cards = cardAliasToId.values().stream().map(x -> (DisplayCardTemplate) templates.getCard(x)).collect(Collectors.toList());

        RawLibraryTemplate presetLibrary = LibraryIO.load("library.json");
        if (presetLibrary == null) {
            presetLibrary = new RawLibraryTemplate();
            presetLibrary.hero = "minions/elderwood_ahri.json";
            presetLibrary.cards = new HashMap<>();
        }
        deckBuilderAppstate = new MyDeckBuilderAppstate(cards, null, cardImages, rootNode, presetLibrary);
        stateManager.attach(deckBuilderAppstate);
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator(assetsPath, FileLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        stateManager.attach(new PostFilterAppstate());
        stateManager.attach(new CameraAppState());
        stateManager.attach(new ButtonAppstate());
        stateManager.attach(new HudTextAppstate(guiNode, guiFont));

        cardImages = new CardImages(assetManager);

        getInputManager().addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        startDeckbuilder();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (stateManager.getState(GameAppstate.class) != null) {
            return;
        }
        if (selectedLibrary == null) {
            if (deckBuilderAppstate == null) {
                startDeckbuilder();
            } else if (deckBuilderAppstate.getResult() != null) {
                selectedLibrary = deckBuilderAppstate.getResult();
                LibraryIO.store("library.json", selectedLibrary);
                stateManager.detach(deckBuilderAppstate);
                deckBuilderAppstate = null;
            }
        }
        if (selectedLibrary == null) {
            return;
        }

        if (selectedStrength == null) {
            SelectOpponentAppstate selectOpponentAppstate = stateManager.getState(SelectOpponentAppstate.class);
            if (selectOpponentAppstate == null) {
                stateManager.attach(new SelectOpponentAppstate(rootNode, assetManager));
            } else if (selectOpponentAppstate.getSelectedStrength() != null) {
                selectedStrength = selectOpponentAppstate.getSelectedStrength();
                stateManager.detach(selectOpponentAppstate);
            }
        }
        if (selectedStrength == null) {
            return;
        }
        if (futureGameReplayService == null) {
            requestGame();
        }

        if (futureGameReplayService.isDone()) {
            stateManager.getState(HudTextAppstate.class).setText("");
            GameReplayService gameReplayService;
            try {
                gameReplayService = futureGameReplayService.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            stateManager.attach(new GameAppstate(client::requestMove, gameReplayService, authentication, cardImages, rootNode));
            futureGameReplayService = null;
        }
    }

    private <T> T load(String path, Class<T> type) {
        return new Gson().fromJson(assetManager.loadAsset(new AssetKey<JsonElement>(path)), type);
    }

}
