package com.etherblood.ethercards.templates.api.setup;

import com.etherblood.ethercards.rules.setup.GameSetup;
import com.etherblood.ethercards.rules.setup.PlayerSetup;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public record RawGameSetup(
        int teamCount,
        RawPlayerSetup[] players,
        String theCoinAlias,
        int startingPlayersHandCardCount,
        int otherPlayersHandCardCount
) {
    public RawGameSetup(RawPlayerSetup[] players, String theCoinAlias) {
        this(2, players, theCoinAlias);
    }

    public RawGameSetup(int teamCount, RawPlayerSetup[] players, String theCoinAlias) {
        this(teamCount, players, theCoinAlias, 4, 5);
    }

    public RawGameSetup(RawGameSetup other) {
        this(other.teamCount,
                Arrays.stream(other.players).map(RawPlayerSetup::new).toArray(RawPlayerSetup[]::new),
                other.theCoinAlias,
                other.startingPlayersHandCardCount,
                other.otherPlayersHandCardCount);
    }


    public GameSetup toGameSetup(ToIntFunction<String> cardAliasResolver) {
        GameSetup setup = new GameSetup();
        setup.teamCount = teamCount;
        setup.theCoinId = theCoinAlias == null ? null : cardAliasResolver.applyAsInt(theCoinAlias);
        setup.players = new PlayerSetup[players.length];
        setup.startingPlayersHandCardCount = startingPlayersHandCardCount;
        setup.otherPlayersHandCardCount = otherPlayersHandCardCount;
        for (int i = 0; i < setup.players.length; i++) {
            setup.players[i] = players[i].toPlayerSetup(cardAliasResolver);
        }
        //validation
        Set<Integer> teams = new HashSet<>();
        for (PlayerSetup player : setup.players) {
            teams.add(player.teamIndex);
        }
        assert teamCount == teams.size();

        return setup;
    }
}
