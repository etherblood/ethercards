package com.etherblood.a.library.builder.ai;

import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.GameSettingsBuilder;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.templates.api.TemplatesLoader;
import com.etherblood.a.templates.api.TemplatesParser;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.etherblood.a.templates.implementation.TemplateAliasMapsImpl;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleSetup {

    private final Random random;
    private final String templatesPath;

    public BattleSetup(String templatesPath, Random random) {
        this.templatesPath = templatesPath;
        this.random = new SecureRandom();
    }

    public Game startGame(RawLibraryTemplate a, RawLibraryTemplate b) {
        GameSettingsBuilder settingsBuilder = new GameSettingsBuilder();
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        settingsBuilder.components = componentsBuilder.build();
        TemplatesLoader loader = new TemplatesLoader(x -> TemplatesLoader.loadFile(templatesPath + "cards/" + x + ".json"), new TemplatesParser(settingsBuilder.components, new TemplateAliasMapsImpl()));

        int indexA = random.nextInt(2);
        
        List<RawPlayerSetup> players = new ArrayList<>();
        RawPlayerSetup playerA = new RawPlayerSetup();
        playerA.library = a;
        playerA.teamIndex = indexA;
        players.add(playerA);
        RawPlayerSetup playerB = new RawPlayerSetup();
        playerB.library = b;
        playerB.teamIndex = 1 - indexA;
        players.add(playerB);

        RawGameSetup gameSetup = new RawGameSetup();
        gameSetup.players = players.toArray(new RawPlayerSetup[players.size()]);
        gameSetup.teamCount = 2;
        gameSetup.theCoinAlias = "the_coin";

        loader.parseLibrary(a);
        loader.parseLibrary(b);
        loader.registerCardAlias(gameSetup.theCoinAlias);

        settingsBuilder.templates = loader.buildGameTemplates();
        GameSettings settings = settingsBuilder.build();

        EntityData data = new SimpleEntityData(settings.components);
        MoveService moves = new MoveService(data, settings.templates, HistoryRandom.producer(random::nextInt), new NoopGameEventListener());
        Game game = new Game(settings, data, moves);
        gameSetup.toGameSetup(loader::registerCardAlias).setup(data, game.getTemplates());
        moves.apply(new Start());
        return game;
    }

    public Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(data, game.getSettings().templates, HistoryRandom.producer(random::nextInt), null, false, false, new NoopGameEventListener());
        return new Game(game.getSettings(), data, moves);
    }
}
