package com.etherblood.a.templates.api.setup;

import com.etherblood.a.rules.setup.GameSetup;
import com.etherblood.a.rules.setup.PlayerSetup;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public class RawGameSetup {

    public int teamCount = 2;
    public RawPlayerSetup[] players;
    public String theCoinAlias;
    public int startingPlayersHandCardCount = 3;
    public int otherPlayersHandCardCount = 4;

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
