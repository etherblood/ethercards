package com.etherblood.a.server.matchmaking;

import com.etherblood.a.network.api.jwt.JwtAuthenticationUser;
import com.etherblood.a.network.api.messages.matchmaking.GameRequest;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import java.util.stream.IntStream;
import org.bouncycastle.util.Arrays;

public class MatchmakeRequest {

    // user info
    public final JwtAuthenticationUser user;
    public final int connectionId;
    public final RawLibraryTemplate library;

    //match settings
    public final int strength;
    public final int[] teamHumanCounts;
    public final int teamSize;

    //computed match settings
    public final int teamCount;
    public final int humanCount;

    public MatchmakeRequest(JwtAuthenticationUser user, int connectionId, RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        this.user = user;
        this.connectionId = connectionId;
        this.library = library;
        this.strength = strength;
        this.teamHumanCounts = teamHumanCounts;
        this.teamSize = teamSize;
        this.teamCount = teamHumanCounts.length;
        this.humanCount = IntStream.of(teamHumanCounts).sum();
    }

    public boolean isMatch(MatchmakeRequest other) {
        return teamSize == other.teamSize && strength == other.strength && Arrays.areEqual(teamHumanCounts, other.teamHumanCounts);
    }

    public static MatchmakeRequest of(GameRequest request, int connectionId, JwtAuthenticationUser user) {
        return new MatchmakeRequest(user, connectionId, request.library, request.strength, Arrays.clone(request.teamHumanCounts), request.teamSize);
    }

}
