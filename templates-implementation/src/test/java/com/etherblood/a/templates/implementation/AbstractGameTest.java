package com.etherblood.a.templates.implementation;

import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.GameSettingsBuilder;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.etherblood.a.templates.api.TemplatesParser;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.google.gson.Gson;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractGameTest {

    private static final String DEFAULT_HERO = "lots_of_health";
    private final TemplatesLoader loader;
    private final RawLibraryTemplate rawLibrary;
    public final GameSettings settings;
    public final CoreComponents core;
    public final GameTemplates templates;

    public EntityData data;
    public MoveService moves;
    public Game game;
    public EffectiveStatsService effectiveStats;

    public AbstractGameTest() {
        TemplateAliasMaps templateAliasMaps = new TemplateAliasMaps();
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        loader = new TemplatesLoader(x -> TemplatesLoader.loadFile("../assets/templates/cards/" + x + ".json"), new TemplatesParser(settingsBuilder.components, templateAliasMaps.getEffects(), templateAliasMaps.getStatModifiers()));
        String[] cardPool = new Gson().fromJson(TemplatesLoader.loadFile("../assets/templates/card_pool.json"), String[].class);
        for (String card : cardPool) {
            loader.registerCardAlias(card);
        }
        loader.registerCardAlias(DEFAULT_HERO);
        templates = loader.buildGameTemplates();

        rawLibrary = new RawLibraryTemplate();
        rawLibrary.hero = DEFAULT_HERO;
        rawLibrary.cards = Collections.emptyMap();
        settingsBuilder.templates = templates;
        settings = settingsBuilder.build();

        core = settings.components.getModule(CoreComponents.class);
    }

    @BeforeEach
    public void before() {
        data = new SimpleEntityData(settings.components);
        moves = new MoveService(settings, data, HistoryRandom.producer(), new NoopGameEventListener());
        game = new Game(settings, data, moves);
        effectiveStats = new EffectiveStatsService(data, templates);

        startGame();
    }

    private void startGame() {
        RawPlayerSetup playerSetup0 = new RawPlayerSetup();
        playerSetup0.library = rawLibrary;
        playerSetup0.teamIndex = 0;
        RawPlayerSetup playerSetup1 = new RawPlayerSetup();
        playerSetup1.library = rawLibrary;
        playerSetup1.teamIndex = 1;

        RawGameSetup gameSetup = new RawGameSetup();
        gameSetup.players = new RawPlayerSetup[]{playerSetup0, playerSetup1};
        gameSetup.teamCount = 2;
        gameSetup.theCoinAlias = null;
        gameSetup.startingPlayersHandCardCount = 0;
        gameSetup.otherPlayersHandCardCount = 0;

        gameSetup.toGameSetup(loader::registerCardAlias).setup(data, templates);
        data.set(player(0), core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK);

        data.remove(hero(0), core.DRAWS_PER_TURN);
        data.remove(hero(0), core.MANA_POOL_AURA_GROWTH);
        data.remove(hero(1), core.DRAWS_PER_TURN);
        data.remove(hero(1), core.MANA_POOL_AURA_GROWTH);
        
        moves.apply(new Update());
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
        return data.list(core.HERO).stream().filter(x -> data.hasValue(x, core.OWNER, player)).findAny().getAsInt();
    }

    public int createCard(int owner, String cardTemplate, int zone) {
        return createCard(owner, getCardId(cardTemplate), zone);
    }

    public int createCard(int owner, int cardTemplate, int zone) {
        assert zone == core.IN_HAND_ZONE || zone == core.IN_GRAVEYARD_ZONE || zone == core.IN_LIBRARY_ZONE;
        int card = SystemsUtil.createCard(data, cardTemplate, owner);
        data.set(card, zone, 1);
        return card;
    }

    public int createMinion(int owner, String minionTemplate) {
        return createMinion(owner, getCardId(minionTemplate));
    }

    public int createMinion(int owner, int minionTemplate) {
        return SystemsUtil.createMinion(data, templates, minionTemplate, owner);
    }

    public int getCardId(String alias) {
        return loader.registerCardAlias(alias);
    }
}
