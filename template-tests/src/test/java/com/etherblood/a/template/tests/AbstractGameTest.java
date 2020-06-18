package com.etherblood.a.template.tests;

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
    private final GameSettings settings;
    private final TemplatesLoader loader;
    public final CoreComponents core;

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
        System.out.println("before");
    }

    @AfterEach
    public void after() {
        System.out.println("after");
    }

    public Game game() {
        EntityData data = new SimpleEntityData(settings.components);
        MoveService moves = new MoveService(settings, data, HistoryRandom.producer());
        Game game = new Game(settings, data, moves);

        SimpleSetup setup = new SimpleSetup(2);
        setup.setLibrary(0, new IntList());
        setup.setLibrary(1, new IntList());
        setup.setHero(0, getMinionId(DEFAULT_HERO));
        setup.setHero(1, getMinionId(DEFAULT_HERO));

        setup.apply(game);
        for (int player : data.list(core.DRAW_CARDS)) {
            data.remove(player, core.DRAW_CARDS);
        }
        data.set(game.findPlayerByIndex(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE);
        return game;
    }

    public int player(Game game, int index) {
        return game.findPlayerByIndex(index);
    }

    public int hero(Game game, int index) {
        int player = player(game, index);
        return game.getData().list(core.HERO).stream().filter(x -> game.getData().hasValue(x, core.OWNED_BY, player)).findAny().getAsInt();
    }

    public int summon(Game game, String minionTemplate, int owner) {
        return summon(game, getMinionId(minionTemplate), owner);
    }

    public int summon(Game game, int minionTemplate, int owner) {
        return SystemsUtil.summon(settings, game.getData(), minionTemplate, owner);
    }

    public int getCardId(String alias) {
        return loader.registerCardAlias(alias);
    }

    public int getMinionId(String alias) {
        return loader.registerMinionAlias(alias);
    }
}
