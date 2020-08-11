package com.etherblood.a.game.server.matchmaking;

import com.etherblood.a.game.server.GamePlayerMapping;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MatchmakeResult {

    public final UUID gameId;
    public final RawGameSetup setup;
    public final List<GamePlayerMapping> playerMappings;
    public final List<BotRequest> botRequests;

    public MatchmakeResult(UUID gameId, RawGameSetup setup, List<GamePlayerMapping> playerMappings, List<BotRequest> botRequests) {
        this.gameId = gameId;
        this.setup = setup;
        this.playerMappings = Collections.unmodifiableList(playerMappings);
        this.botRequests = Collections.unmodifiableList(botRequests);
    }
}
