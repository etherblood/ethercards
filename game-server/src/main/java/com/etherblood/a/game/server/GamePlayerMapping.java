package com.etherblood.a.game.server;

import com.etherblood.a.network.api.jwt.JwtAuthenticationUser;
import java.util.UUID;

public class GamePlayerMapping {

    public final UUID gameId;
    public final JwtAuthenticationUser user;
    public final int playerIndex;
    public Integer connectionId;

    public GamePlayerMapping(UUID gameId, JwtAuthenticationUser user, int playerIndex, Integer connectionId) {
        this.gameId = gameId;
        this.user = user;
        this.playerIndex = playerIndex;
        this.connectionId = connectionId;
    }
}
