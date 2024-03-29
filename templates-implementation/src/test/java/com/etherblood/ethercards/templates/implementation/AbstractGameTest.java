package com.etherblood.ethercards.templates.implementation;

import com.etherblood.ethercards.entities.ComponentsBuilder;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.SimpleEntityData;
import com.etherblood.ethercards.game.events.api.NoopGameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.EffectiveStatsService;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.GameSettings;
import com.etherblood.ethercards.rules.GameSettingsBuilder;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.HistoryRandom;
import com.etherblood.ethercards.rules.MoveService;
import com.etherblood.ethercards.rules.PlayerPhase;
import com.etherblood.ethercards.rules.classic.GameLoopService;
import com.etherblood.ethercards.rules.moves.Update;
import com.etherblood.ethercards.rules.setup.GameSetup;
import com.etherblood.ethercards.rules.updates.SystemsUtil;
import com.etherblood.ethercards.rules.updates.ZoneService;
import com.etherblood.ethercards.templates.api.RecordTypeAdapterFactory;
import com.etherblood.ethercards.templates.api.TemplatesLoader;
import com.etherblood.ethercards.templates.api.TemplatesParser;
import com.etherblood.ethercards.templates.api.setup.RawGameSetup;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.etherblood.ethercards.templates.api.setup.RawPlayerSetup;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractGameTest {

    private static final String DEFAULT_HERO = "elderwood_ahri";
    private final TemplatesLoader loader;
    private final RawLibraryTemplate rawLibrary;
    public final GameSettings settings;
    public final CoreComponents core;
    public final GameTemplates templates;
    private final GameSetup setup;

    public EntityData data;
    public HistoryRandom random;
    public NoopGameEventListener events;
    public MoveService moves;
    public Game game;
    public EffectiveStatsService effectiveStats;
    public ZoneService zoneService;

    public AbstractGameTest() {
        this(1, 1);
    }

    public AbstractGameTest(int... teamSizes) {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        loader = new TemplatesLoader(x -> TemplatesLoader.loadFile("../assets/templates/cards/" + x + ".json"), new TemplatesParser(settingsBuilder.components, new TemplateAliasMapsImpl()));
        String[] cardPool = new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create().fromJson(TemplatesLoader.loadFile("../assets/templates/card_pool.json"), String[].class);
        for (String card : cardPool) {
            loader.registerCardAlias(card);
        }
        loader.registerCardAlias(DEFAULT_HERO);
        templates = loader.buildGameTemplates();

        rawLibrary = new RawLibraryTemplate(DEFAULT_HERO, Collections.emptyMap());
        settingsBuilder.templates = templates;
        settings = settingsBuilder.build();
        core = settings.components.getModule(CoreComponents.class);

        List<RawPlayerSetup> players = new ArrayList<>();
        for (int teamIndex = 0; teamIndex < teamSizes.length; teamIndex++) {
            int teamSize = teamSizes[teamIndex];
            for (int playerTeamIndex = 0; playerTeamIndex < teamSize; playerTeamIndex++) {
                RawPlayerSetup player = new RawPlayerSetup(0, null, teamIndex, rawLibrary);
                players.add(player);
            }
        }

        RawGameSetup gameSetup = new RawGameSetup(
                teamSizes.length,
                players.toArray(RawPlayerSetup[]::new),
                null,
                0,
                0);
        setup = gameSetup.toGameSetup(loader::registerCardAlias);
    }

    @BeforeEach
    public void before() {
        data = new SimpleEntityData(settings.components);
        random = HistoryRandom.producer(new Random(7)::nextInt);
        events = new NoopGameEventListener();
        moves = new MoveService(data, settings.templates, random, events, new GameLoopService(data, templates, random, events));
        game = new Game(settings, data, moves);
        effectiveStats = new EffectiveStatsService(data, templates);
        zoneService = new ZoneService(data, templates, random, events);

        startGame();
    }

    private void startGame() {
        setup.setup(data, templates);
        for (int hero : data.list(core.HERO)) {
            data.remove(hero, core.DRAWS_PER_TURN);
            data.remove(hero, core.MANA_POOL_AURA_GROWTH);
        }
        int team0 = team(0);
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.TEAM, team0)) {
                data.set(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK);
            }
        }
        data.set(team0, core.ACTIVE_TEAM_PHASE, PlayerPhase.ATTACK);
        moves.apply(new Update());
    }

    @AfterEach
    public void after() {
        data = null;
        moves = null;
        game = null;
    }

    public int teamCount() {
        return setup.teamCount;
    }

    public int playerCount() {
        return setup.players.length;
    }

    public int team(int index) {
        return data.findByValue(core.TEAM_INDEX, index).get(0);
    }

    public int player(int index) {
        return game.findPlayerByIndex(index);
    }

    public int hero(int index) {
        int player = player(index);
        return data.list(core.HERO).stream().filter(x -> data.hasValue(x, core.OWNER, player)).findAny().getAsInt();
    }

    public int createCard(int owner, String cardTemplate, int zone) {
        return createCard(owner, getAliasId(cardTemplate), zone);
    }

    public int createCard(int owner, int cardTemplate, int zone) {
        assert zone == core.IN_HAND_ZONE || zone == core.IN_GRAVEYARD_ZONE || zone == core.IN_LIBRARY_ZONE;
        int card = SystemsUtil.createCard(data, cardTemplate, owner);

        if (zone == core.IN_HAND_ZONE) {
            zoneService.addToHand(card);
        } else if (zone == core.IN_LIBRARY_ZONE) {
            zoneService.addToLibrary(card);
        } else if (zone == core.IN_GRAVEYARD_ZONE) {
            zoneService.addToGraveyard(card);
        } else {
            throw new AssertionError(zone);
        }
        return card;
    }

    public int createMinion(int owner, String minionTemplate) {
        return createMinion(owner, getAliasId(minionTemplate));
    }

    public int createMinion(int owner, int minionTemplate) {
        return SystemsUtil.createMinion(data, templates, random, events, minionTemplate, owner);
    }

    public int getAliasId(String alias) {
        return loader.getAliasId(alias);
    }
}
