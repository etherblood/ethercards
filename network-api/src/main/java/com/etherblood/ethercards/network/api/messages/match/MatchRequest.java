package com.etherblood.ethercards.network.api.messages.match;

import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;

public record MatchRequest(
        RawLibraryTemplate library,
        int strength,
        int[] teamHumanCounts,
        int teamSize
) {
}
