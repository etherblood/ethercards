package com.etherblood.a.server;

import java.util.UUID;

public class GamePlayerMapping {

    public final UUID gameId;
    public final long playerId;
    public final int playerIndex;
    public final int connectionId;

    public GamePlayerMapping(UUID gameId, long playerId, int playerIndex, int connectionId) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerIndex = playerIndex;
        this.connectionId = connectionId;
    }
}
