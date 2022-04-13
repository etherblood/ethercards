package com.etherblood.ethercards.ai.library.builder;

import com.etherblood.ethercards.entities.ComponentsBuilder;
import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.SimpleEntityData;
import com.etherblood.ethercards.game.events.api.NoopGameEventListener;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.GameSettings;
import com.etherblood.ethercards.rules.GameSettingsBuilder;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.HistoryRandom;
import com.etherblood.ethercards.rules.MoveService;
import com.etherblood.ethercards.rules.classic.GameLoopService;
import com.etherblood.ethercards.rules.moves.Start;
import com.etherblood.ethercards.templates.api.TemplatesLoader;
import com.etherblood.ethercards.templates.api.TemplatesParser;
import com.etherblood.ethercards.templates.api.setup.RawGameSetup;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.etherblood.ethercards.templates.api.setup.RawPlayerSetup;
import com.etherblood.ethercards.templates.implementation.TemplateAliasMapsImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleSetup {

    private final Random random;
    private final String templatesPath;

    public BattleSetup(String templatesPath, Random random) {
        this.templatesPath = templatesPath;
        this.random = random;
    }

    public Game startGame(RawLibraryTemplate a, RawLibraryTemplate b) {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        TemplatesLoader loader = new TemplatesLoader(x -> TemplatesLoader.loadFile(templatesPath + "cards/" + x + ".json"), new TemplatesParser(settingsBuilder.components, new TemplateAliasMapsImpl()));

        int indexA = random.nextInt(2);

        List<RawPlayerSetup> players = new ArrayList<>();
        RawPlayerSetup playerA = new RawPlayerSetup(
                0, null, indexA, a
        );
        players.add(playerA);
        RawPlayerSetup playerB = new RawPlayerSetup(
                1 - indexA, null, indexA, b
        );
        players.add(playerB);

        RawGameSetup gameSetup = new RawGameSetup(
                players.toArray(RawPlayerSetup[]::new),
                "the_coin");

        loader.parseLibrary(a);
        loader.parseLibrary(b);
        loader.registerCardAlias(gameSetup.theCoinAlias());

        settingsBuilder.templates = loader.buildGameTemplates();
        GameSettings settings = settingsBuilder.build();

        EntityData data = new SimpleEntityData(settings.components);
        NoopGameEventListener eventListener = new NoopGameEventListener();
        HistoryRandom producer = HistoryRandom.producer(random::nextInt);
        MoveService moves = new MoveService(data, settings.templates, producer, eventListener, new GameLoopService(data, settings.templates, producer, eventListener));
        Game game = new Game(settings, data, moves);
        gameSetup.toGameSetup(loader::registerCardAlias).setup(data, game.getTemplates());
        moves.apply(new Start());
        return game;
    }

    public Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        NoopGameEventListener eventListener = new NoopGameEventListener();
        GameTemplates templates = game.getSettings().templates;
        HistoryRandom producer = HistoryRandom.producer(random::nextInt);
        MoveService moves = new MoveService(data, templates, producer, null, false, false, eventListener, new GameLoopService(data, templates, producer, eventListener));
        return new Game(game.getSettings(), data, moves);
    }
}
