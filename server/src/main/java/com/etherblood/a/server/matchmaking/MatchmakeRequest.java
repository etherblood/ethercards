package com.etherblood.a.server.matchmaking;

import com.etherblood.a.network.api.jwt.JwtAuthenticationUser;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.network.api.matchmaking.GameRequest;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import java.util.stream.IntStream;
import org.bouncycastle.util.Arrays;

public class MatchmakeRequest {

    // user info
    public final long userId;
    public final int connectionId;
    public final String name;
    public final RawLibraryTemplate library;

    //match settings
    public final int strength;
    public final int[] teamHumanCounts;
    public final int teamSize;

    //computed match settings
    public final int teamCount;
    public final int humanCount;

    public MatchmakeRequest(long userId, int connectionId, String name, RawLibraryTemplate library, int strength, int[] teamHumanCounts, int teamSize) {
        this.userId = userId;
        this.connectionId = connectionId;
        this.name = name;
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

    public static MatchmakeRequest of(GameRequest request, int connectionId, JwtParser jwtParser) {
        JwtAuthenticationUser user = jwtParser.verify(request.jwt).user;
        return new MatchmakeRequest(user.id, connectionId, user.login, request.library, request.strength, Arrays.clone(request.teamHumanCounts), request.teamSize);
    }

}
