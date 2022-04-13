package com.etherblood.ethercards.network.api.messages.match;

import com.etherblood.ethercards.network.api.GameReplay;

public record ConnectedToMatchUpdate(
        GameReplay replay,
        int playerIndex
) {
}
