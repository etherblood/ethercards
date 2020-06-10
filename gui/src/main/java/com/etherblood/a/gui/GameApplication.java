package com.etherblood.a.gui;

import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.moves.Move;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapFont;
import java.util.function.Consumer;

public class GameApplication extends SimpleApplication {

    private final String assetsPath;
    private final GameReplayService gameReplayService;
    private final Consumer<Move> moveApplier;
    private final int playerIndex;
    private Game game;

    public GameApplication(String assetsPath, GameReplayService gameReplayService, Consumer<Move> moveApplier, int playerIndex) {
        this.assetsPath = assetsPath;
        this.gameReplayService = gameReplayService;
        this.moveApplier = moveApplier;
        this.playerIndex = playerIndex;
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLocator(assetsPath, FileLocator.class);
        assetManager.registerLoader(GsonLoader.class, "json");

        initGame();
        stateManager.attach(new GameBoardAppstate(this, game.findPlayerByIndex(playerIndex)));
    }

    @Override
    public void simpleUpdate(float tpf) {
        gameReplayService.updateInstance(game);
    }

    private void initGame() {
        game = gameReplayService.createInstance();
    }

    public void applyMove(Move move) {
        moveApplier.accept(move);
    }

    public BitmapFont getGuiFont() {
        return guiFont;
    }

    public Game getGame() {
        return game;
    }

    public String getPlayerName(int playerIndex) {
        return gameReplayService.getPlayerName(playerIndex);
    }
}
