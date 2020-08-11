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
import com.jme3.input.controls.ActionListener;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameApplication extends SimpleApplication {

    private final JwtAuthentication authentication;
    private final String version;
    private final String assetsPath;
    private final String hostUrl;
    private final boolean battleFullArt;
    private CardImages cardImages;
    private GameClient client;

    private MatchOpponents selectedOpponents;
    private RawLibraryTemplate selectedLibrary;

    public GameApplication(Properties properties, JwtAuthentication authentication, String version) {
        this.authentication = authentication;
        this.assetsPath = properties.getProperty("assets");
        this.hostUrl = properties.getProperty("hostUrl");
        this.battleFullArt = Boolean.parseBoolean(properties.getProperty("battleFullArt"));
        this.version = version;
    }

    private void requestGame() {
        client.requestGame(selectedLibrary, selectedOpponents.strength, selectedOpponents.teamHumanCounts, selectedOpponents.teamSize);
        stateManager.getState(HudTextAppstate.class).setText("Waiting for opponent...");

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
            presetLibrary.hero = migrateAlias("elderwood_ahri");
            presetLibrary.cards = presetLibrary.cards.entrySet().stream().collect(Collectors.toMap(x -> migrateAlias(x.getKey()), x -> x.getValue()));
        }
        stateManager.attach(new MyDeckBuilderAppstate(cards, cardImages, rootNode, guiNode, presetLibrary, components));
    }

    private static String migrateAlias(String alias) {
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

        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        inputManager.addMapping("toggleMenu", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("f1", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    toggleMenu();
                }
            }

        }, "toggleMenu");
        startDeckbuilder();

        Function<String, JsonElement> assetLoader = x -> load("templates/cards/" + x + ".json", JsonElement.class);
        client = new GameClient(assetLoader, version);
        try {
            client.start(hostUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        client.identify(authentication.rawJwt);
    }

    @Override
    public void simpleUpdate(float tpf) {
        GameAppstate gameAppstate = stateManager.getState(GameAppstate.class);
        if (gameAppstate != null) {
            if (gameAppstate.isGameCompleted()) {
                stateManager.detach(gameAppstate);
                client.resetGame();
            }
            return;
        }
        if (client.getGame() == null) {
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

            if (!client.isGameRequested()) {
                requestGame();
            }
        } else {
            stateManager.getState(HudTextAppstate.class).setText("");
            GameReplayView gameReplayService = client.getGame();
            stateManager.attach(new GameAppstate(client::requestMove, gameReplayService.gameReplay, cardImages, rootNode, assetsPath, battleFullArt, gameReplayService.playerIndex));
            selectedLibrary = null;
            selectedOpponents = null;

            SelectOpponentAppstate selectOpponentAppstate = stateManager.getState(SelectOpponentAppstate.class);
            if (selectOpponentAppstate != null) {
                stateManager.detach(selectOpponentAppstate);
            }

            MyDeckBuilderAppstate deckBuilderAppstate = stateManager.getState(MyDeckBuilderAppstate.class);
            if (deckBuilderAppstate != null) {
                if (deckBuilderAppstate.getResult() != null) {
                    LibraryIO.store("library.json", deckBuilderAppstate.getResult());
                }
                stateManager.detach(deckBuilderAppstate);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        client.stop();
    }

    private <T> T load(String path, Class<T> type) {
        return new Gson().fromJson(assetManager.loadAsset(new AssetKey<JsonElement>(path)), type);
    }

    private void toggleMenu() {
        MenuAppstate state = stateManager.getState(MenuAppstate.class);
        if (state != null) {
            stateManager.detach(state);
        } else {
            stateManager.attach(new MenuAppstate(guiNode));
        }
    }

}
