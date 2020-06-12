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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameApplication extends SimpleApplication {

    private final JwtAuthentication authentication;
    private final String assetsPath;
    private final String hostUrl;
    private CardImages cardImages;
    private final int strength;
    private MyDeckBuilderAppstate deckBuilderAppstate;
    private Future<GameReplayService> futureGameReplayService;
    private GameClient client;

    public GameApplication(Properties properties, JwtAuthentication authentication) throws Exception {
        this.authentication = authentication;
        this.assetsPath = properties.getProperty("assets");
        this.hostUrl = properties.getProperty("hostUrl");
        System.out.println("Please enter 0 for a human opponent or select bot strength (1-10)");
        strength = Integer.parseInt(new Scanner(System.in).nextLine());
    }

    private void requestGame(RawLibraryTemplate library) {
        Function<String, JsonElement> assetLoader = x -> load("templates/" + x, JsonElement.class);

        client = new GameClient(assetLoader);
        try {
            client.start(hostUrl);
            if (strength == 0) {
                futureGameReplayService = client.requestGame(authentication.rawJwt, library);
            } else if (strength > 0) {
                futureGameReplayService = client.requestBotgame(authentication.rawJwt, library, strength * 10_000);
            } else {
                throw new IllegalArgumentException(Integer.toString(strength));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void startDeckbuilder() {
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);

        TemplatesLoader templatesLoader = new TemplatesLoader(x -> load("templates/" + x, JsonElement.class), new TemplatesParser(componentsBuilder.build()));
        String[] cardAliases = load("templates/card_list.json", String[].class);
        Map<String, Integer> cardAliasToId = new LinkedHashMap<>();
        for (String card : cardAliases) {
            int id = templatesLoader.registerCardAlias(card);
            cardAliasToId.put(card, id);
        }
        GameTemplates templates = templatesLoader.buildGameTemplates();
        List<DisplayCardTemplate> cards = cardAliasToId.values().stream().map(x -> (DisplayCardTemplate) templates.getCard(x)).collect(Collectors.toList());

        RawLibraryTemplate presetLibrary = load("templates/libraries/default.json", RawLibraryTemplate.class);
        deckBuilderAppstate = new MyDeckBuilderAppstate(cards, null, cardImages, rootNode, presetLibrary);
        stateManager.attach(deckBuilderAppstate);
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator(assetsPath, FileLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        stateManager.attach(new PostFilterAppstate());
        stateManager.attach(new CameraAppState());

        cardImages = new CardImages(assetManager);

        getInputManager().addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        startDeckbuilder();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (deckBuilderAppstate != null && deckBuilderAppstate.getResult().isDone()) {
            try {
                requestGame(deckBuilderAppstate.getResult().get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            stateManager.detach(deckBuilderAppstate);
            deckBuilderAppstate = null;
        }

        if (futureGameReplayService != null && futureGameReplayService.isDone()) {
            GameReplayService gameReplayService;
            try {
                gameReplayService = futureGameReplayService.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            stateManager.attach(new GameBoardAppstate(client::requestMove, gameReplayService, authentication, strength, cardImages, rootNode, guiNode, guiFont));
            futureGameReplayService = null;
        }
    }

    private <T> T load(String path, Class<T> type) {
        return new Gson().fromJson(assetManager.loadAsset(new AssetKey<JsonElement>(path)), type);
    }

}
