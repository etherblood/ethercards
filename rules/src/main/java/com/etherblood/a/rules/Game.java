package com.etherblood.a.rules;

import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.EntityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Game {

    private static final Logger LOG = LoggerFactory.getLogger(Game.class);

    private final GameSettings settings;
    private final EntityData data;
    private final MoveService moves;
    private final CoreComponents core;

    public Game(GameSettings settings, EntityData data, MoveService moves) {
        this.settings = Objects.requireNonNull(settings, "Settings must not be null.");
        this.data = Objects.requireNonNull(data);
        this.moves = Objects.requireNonNull(moves);
        Components components = data.getComponents();
        core = Objects.requireNonNull(components.getModule(CoreComponents.class), "Core component module missing.");
    }

    public EntityData getData() {
        return data;
    }

    public GameTemplates getTemplates() {
        return settings.templates;
    }

    public MoveService getMoves() {
        return moves;
    }

    public int findPlayerByIndex(int playerIndex) {
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.PLAYER_INDEX, playerIndex)) {
                return player;
            }
        }
        throw new AssertionError();
    }

    public int getPlayerIndex(int player) {
        return data.get(player, core.PLAYER_INDEX);
    }

    public boolean isPlayerActive(int player) {
        return data.has(player, core.ACTIVE_PLAYER_PHASE);
    }

    public boolean isGameOver() {
        //TODO: called very often, can performance be improved?
        return data.list(core.PLAYER_INDEX).size() == data.list(core.PLAYER_RESULT).size();
    }

    public boolean hasPlayerWon(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.WIN);
    }

    public boolean hasPlayerLost(int player) {
        return data.hasValue(player, core.PLAYER_RESULT, PlayerResult.LOSS);
    }

    public GameSettings getSettings() {
        return settings;
    }

}
