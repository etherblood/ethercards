package com.etherblood.a.gui;

import com.etherblood.a.gui.matchmaking.SelectOpponentAppstate;
import com.etherblood.a.client.GameClient;
import com.etherblood.a.client.GameReplayView;
import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.gui.matchmaking.MatchOpponents;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.gui.soprettyboard.ForestBoardAppstate;
import com.etherblood.a.gui.soprettyboard.PostFilterAppstate;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.DisplayCardTemplate;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.etherblood.a.templates.api.TemplatesParser;
import com.etherblood.a.templates.implementation.TemplateAliasMapsImpl;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;
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
    private final boolean battleFullArt;
    private CardImages cardImages;
    private Future<GameReplayView> futureGameReplayService;
    private GameClient client;

    private MatchOpponents selectedOpponents;
    private RawLibraryTemplate selectedLibrary;

    public GameApplication(Properties properties, JwtAuthentication authentication) {
        this.authentication = authentication;
        this.assetsPath = properties.getProperty("assets");
        this.hostUrl = properties.getProperty("hostUrl");
        this.battleFullArt = Boolean.parseBoolean(properties.getProperty("battleFullArt"));
    }

    private void requestGame() {
        Function<String, JsonElement> assetLoader = x -> load("templates/cards/" + x + ".json", JsonElement.class);

        client = new GameClient(assetLoader);
        try {
            client.start(hostUrl);
            client.identify(authentication.rawJwt);
            futureGameReplayService = client.requestGame(selectedLibrary, selectedOpponents.strength, selectedOpponents.teamHumanCounts, selectedOpponents.teamSize);
            stateManager.getState(HudTextAppstate.class).setText("Waiting for opponent...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void startDeckbuilder() {
        stateManager.getState(HudTextAppstate.class).setText("Create your library.\nPress LEFT & RIGHT to navigate pages.");
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        Components components = componentsBuilder.build();

        TemplatesLoader templatesLoader = new TemplatesLoader(x -> load("templates/cards/" + x + ".json", JsonElement.class), new TemplatesParser(components, new TemplateAliasMapsImpl()));
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
            presetLibrary.hero = "elderwood_ahri";
            presetLibrary.cards = new HashMap<>();
        } else {
            presetLibrary.hero = cleanAlias("elderwood_ahri");
            presetLibrary.cards = presetLibrary.cards.entrySet().stream().collect(Collectors.toMap(x -> cleanAlias(x.getKey()), x -> x.getValue()));
        }
        stateManager.attach(new MyDeckBuilderAppstate(cards, cardImages, rootNode, guiNode, presetLibrary, components));
    }

    private static String cleanAlias(String alias) {
        if (alias.endsWith(".json")) {
            alias = alias.substring(0, alias.length() - ".json".length());
        }
        if (alias.startsWith("minions/")) {
            alias = alias.substring("minions/".length());
        }
        if (alias.startsWith("cards/")) {
            alias = alias.substring("cards/".length());
        }
        return alias;
    }

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        String defaultStyleName = "myStyleTest";
        Styles styles = GuiGlobals.getInstance().getStyles();
        QuadBackgroundComponent bg = new QuadBackgroundComponent(ColorRGBA.DarkGray);
        Attributes attrs = styles.getSelector(defaultStyleName);
        attrs.set("color", ColorRGBA.LightGray);
        attrs.set("background", bg);
        attrs.set("fontSize", 50);

//        BaseStyles.loadGlassStyle();// very slow
        styles.setDefaultStyle(defaultStyleName);

        assetManager.registerLocator(assetsPath, FileLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        stateManager.attach(new PostFilterAppstate());
        stateManager.attach(new CameraAppState());
        stateManager.attach(new ButtonAppstate());
        stateManager.attach(new HudTextAppstate(guiNode, guiFont));
        stateManager.attach(new ForestBoardAppstate(rootNode));

        cardImages = new CardImages(assetManager);

        getInputManager().addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        getInputManager().addMapping("f1", new KeyTrigger(KeyInput.KEY_F1));
        getInputManager().addMapping("right", new KeyTrigger(KeyInput.KEY_RIGHT));
        getInputManager().addMapping("left", new KeyTrigger(KeyInput.KEY_LEFT));
        startDeckbuilder();
    }

    @Override
    public void simpleUpdate(float tpf) {
        GameAppstate gameAppstate = stateManager.getState(GameAppstate.class);
        if (gameAppstate != null) {
            if (gameAppstate.isGameCompleted()) {
                selectedLibrary = null;
                selectedOpponents = null;
                futureGameReplayService = null;
                client.stop();
                client = null;
                stateManager.detach(gameAppstate);
            }
            return;
        }
        if (selectedLibrary == null) {
            MyDeckBuilderAppstate deckBuilderAppstate = stateManager.getState(MyDeckBuilderAppstate.class);
            if (deckBuilderAppstate == null) {
                startDeckbuilder();
            } else if (deckBuilderAppstate.getResult() != null) {
                selectedLibrary = deckBuilderAppstate.getResult();
                LibraryIO.store("library.json", selectedLibrary);
                stateManager.detach(deckBuilderAppstate);
            }
        }
        if (selectedLibrary == null) {
            return;
        }

        if (selectedOpponents == null) {
            SelectOpponentAppstate selectOpponentAppstate = stateManager.getState(SelectOpponentAppstate.class);
            if (selectOpponentAppstate == null) {
                stateManager.attach(new SelectOpponentAppstate(guiNode, assetManager));
            } else if (selectOpponentAppstate.getMatchOpponents() != null) {
                selectedOpponents = selectOpponentAppstate.getMatchOpponents();
                stateManager.detach(selectOpponentAppstate);
            }
        }
        if (selectedOpponents == null) {
            return;
        }
        if (futureGameReplayService == null) {
            requestGame();
        }

        if (futureGameReplayService.isDone()) {
            stateManager.getState(HudTextAppstate.class).setText("");
            GameReplayView gameReplayService;
            try {
                gameReplayService = futureGameReplayService.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            stateManager.attach(new GameAppstate(client::requestMove, gameReplayService.gameReplay, cardImages, rootNode, assetsPath, battleFullArt, gameReplayService.playerIndex));
            futureGameReplayService = null;
        }
    }

    private <T> T load(String path, Class<T> type) {
        return new Gson().fromJson(assetManager.loadAsset(new AssetKey<JsonElement>(path)), type);
    }

}
