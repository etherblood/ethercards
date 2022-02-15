package com.etherblood.ethercards.rules.setup;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntMap;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.GameTemplates;
import com.etherblood.ethercards.rules.updates.SystemsUtil;

public class GameSetup {

    public int teamCount;
    public PlayerSetup[] players;
    public Integer theCoinId;
    public int startingPlayersHandCardCount;
    public int otherPlayersHandCardCount;

    public void setup(EntityData data, GameTemplates templates) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);

        int[] teams = new int[teamCount];
        for (int teamIndex = 0; teamIndex < teams.length; teamIndex++) {
            int team = data.createEntity();
            data.set(team, core.TEAM_INDEX, teamIndex);
            teams[teamIndex] = team;
        }

        for (int playerIndex = 0; playerIndex < players.length; playerIndex++) {
            PlayerSetup playerSetup = players[playerIndex];
            IntMap librarySetup = playerSetup.libraryCardCounts;
            int team = teams[playerSetup.teamIndex];

            int player = data.createEntity();
            data.set(player, core.PLAYER_INDEX, playerIndex);
            data.set(player, core.TEAM, team);

            int heroEntity = SystemsUtil.createHero(data, templates, null, null, playerSetup.heroId, player);
            data.set(heroEntity, core.SUMMONING_SICKNESS, 1);

            for (int cardId : librarySetup) {
                int amount = librarySetup.get(cardId);
                for (int i = 0; i < amount; i++) {
                    int card = SystemsUtil.createCard(data, cardId, player);
                    data.set(card, core.IN_LIBRARY_ZONE, 1);
                }
            }

            if (data.hasValue(team, core.TEAM_INDEX, 0)) {
                if (startingPlayersHandCardCount != 0) {
                    data.set(player, core.INITIAL_DRAWS, startingPlayersHandCardCount);
                }
            } else {
                if (otherPlayersHandCardCount != 0) {
                    data.set(player, core.INITIAL_DRAWS, otherPlayersHandCardCount);
                }
                if (theCoinId != null) {
                    int coin = SystemsUtil.createCard(data, theCoinId, player);
                    data.set(coin, core.IN_HAND_ZONE, 1);
                    data.set(coin, core.CANNOT_BE_MULLIGANED, 1);
                }
            }
        }
    }
}
