package com.etherblood.a.game.tests;

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
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.setup.SimpleSetup;
import com.etherblood.a.rules.systems.util.SystemsUtil;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.etherblood.a.templates.TemplatesLoader;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractGameTest {

    private static final String DEFAULT_HERO = "minions/lots_of_health.json";
    private final TemplatesLoader loader;
    public final GameSettings settings;
    public final CoreComponents core;

    public EntityData data;
    public MoveService moves;
    public Game game;

    public AbstractGameTest() {
        Function<String, JsonElement> assetLoader = x -> {
            try ( Reader reader = Files.newBufferedReader(Paths.get("../assets/templates/" + x))) {
                return new Gson().fromJson(reader, JsonElement.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };

        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        loader = new TemplatesLoader(assetLoader, new TemplatesParser(settingsBuilder.components));
        String[] cardPool = new Gson().fromJson(assetLoader.apply("card_pool.json"), String[].class);
        for (String card : cardPool) {
            loader.registerCardAlias(card);
        }
        loader.registerMinionAlias(DEFAULT_HERO);

        RawLibraryTemplate rawLibrary = new RawLibraryTemplate();
        rawLibrary.hero = DEFAULT_HERO;
        rawLibrary.cards = Collections.emptyMap();
        settingsBuilder.templates = loader.buildGameTemplates();
        settings = settingsBuilder.build();

        core = settings.components.getModule(CoreComponents.class);
    }

    @BeforeEach
    public void before() {
        data = new SimpleEntityData(settings.components);
        moves = new MoveService(settings, data, HistoryRandom.producer());
        game = new Game(settings, data, moves);

        SimpleSetup setup = new SimpleSetup(2);
        setup.setLibrary(0, new IntList());
        setup.setLibrary(1, new IntList());
        setup.setHero(0, getMinionId(DEFAULT_HERO));
        setup.setHero(1, getMinionId(DEFAULT_HERO));

        setup.apply(game);
        for (int player : data.list(core.DRAW_CARDS)) {
            data.remove(player, core.DRAW_CARDS);
        }
        for (int hero : data.list(core.DRAWS_PER_TURN)) {
            data.remove(hero, core.DRAWS_PER_TURN);
        }
        data.set(game.findPlayerByIndex(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK);
    }

    @AfterEach
    public void after() {
        data = null;
        moves = null;
        game = null;
    }

    public int player(int index) {
        return game.findPlayerByIndex(index);
    }

    public int hero(int index) {
        int player = player(index);
        return data.list(core.HERO).stream().filter(x -> data.hasValue(x, core.OWNED_BY, player)).findAny().getAsInt();
    }

    public int createLibraryCard(int owner, String cardTemplate) {
        return createLibraryCard(owner, getCardId(cardTemplate));
    }

    public int createLibraryCard(int owner, int cardTemplate) {
        int card = data.createEntity();
        data.set(card, core.CARD_TEMPLATE, cardTemplate);
        data.set(card, core.OWNED_BY, owner);
        data.set(card, core.IN_LIBRARY_ZONE, 1);
        return card;
    }

    public int createHandCard(int owner, String cardTemplate) {
        return createHandCard(owner, getCardId(cardTemplate));
    }

    public int createHandCard(int owner, int cardTemplate) {
        int card = data.createEntity();
        data.set(card, core.CARD_TEMPLATE, cardTemplate);
        data.set(card, core.OWNED_BY, owner);
        data.set(card, core.IN_HAND_ZONE, 1);
        return card;
    }

    public int createMinion(int owner, String minionTemplate) {
        return createMinion(owner, getMinionId(minionTemplate));
    }

    public int createMinion(int owner, int minionTemplate) {
        return SystemsUtil.createMinion(settings, data, game.getMoves().getRandom(), minionTemplate, owner);
    }

    public int getCardId(String alias) {
        return loader.registerCardAlias(alias);
    }

    public int getMinionId(String alias) {
        return loader.registerMinionAlias(alias);
    }
}
