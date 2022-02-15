package com.etherblood.ethercards.ai;

import com.etherblood.ethercards.entities.EntityData;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.CoreComponents;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.PlayerPhase;
import com.etherblood.ethercards.rules.PlayerResult;
import com.etherblood.ethercards.rules.updates.ZoneService;
import java.util.Arrays;
import java.util.Random;

public abstract class BotGameAdapter<T, V extends BotGameAdapter<T, V>> implements BotGame<T, V> {

    protected final Game game;
    protected final CoreComponents core;

    public BotGameAdapter(Game game) {
        this.game = game;
        core = game.getData().getComponents().getModule(CoreComponents.class);
    }

    @Override
    public boolean isGameOver() {
        return game.isGameOver();
    }

    @Override
    public int playerCount() {
        return game.getData().list(core.PLAYER_INDEX).size();
    }

    @Override
    public boolean isPlayerIndexActive(int playerIndex) {
        return game.isPlayerActive(game.findPlayerByIndex(playerIndex));
    }

    @Override
    public float[] resultPlayerScores() {
        if (!isGameOver()) {
            throw new IllegalStateException();
        }
        EntityData data = game.getData();
        IntList playerResults = data.list(core.PLAYER_RESULT);
        IntList winners = new IntList();
        IntList losers = new IntList();
        for (int player : playerResults) {
            if (data.get(player, core.PLAYER_RESULT) == PlayerResult.WIN) {
                winners.add(player);
            } else {
                losers.add(player);
            }
        }
        float[] result = new float[playerCount()];
        if (winners.isEmpty()) {
            Arrays.fill(result, 0.5f);
            return result;
        }
        for (int winner : winners) {
            int index = data.get(winner, core.PLAYER_INDEX);
            result[index] = 1f;
        }
        return result;
    }

    @Override
    public void randomizeHiddenInformation(Random random, int selfIndex) {
        EntityData data = game.getData();
        Integer self = null;
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.PLAYER_INDEX, selfIndex)) {
                self = player;
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN)) {
                // discard mulligans of opponents, they make no difference since all hand cards will be randomized
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
            }
        }
        int team = data.get(self, core.TEAM);
        IntList allHandCards = data.list(core.IN_HAND_ZONE);
        IntList opponentHandCards = new IntList();
        for (int card : allHandCards) {
            if (!data.hasValue(card, core.TEAM, team)) {
                opponentHandCards.add(card);
            }
        }
        ZoneService zoneService = new ZoneService(data, game.getTemplates(), game.getRandom(), game.getEvents());
        for (int card : opponentHandCards) {
            data.remove(card, core.MULLIGAN);
            zoneService.removeFromHand(card);
            zoneService.addToLibrary(card);
        }

        for (int card : opponentHandCards) {
            int owner = data.get(card, core.OWNER);
            IntList allLibraryCards = data.list(core.IN_LIBRARY_ZONE);
            IntList ownerLibraryCards = new IntList();
            for (int libraryCard : allLibraryCards) {
                if (data.hasValue(libraryCard, core.OWNER, owner)) {
                    ownerLibraryCards.add(libraryCard);
                }
            }
            int handCard = ownerLibraryCards.get(random.nextInt(ownerLibraryCards.size()));
            zoneService.removeFromLibrary(handCard);
            zoneService.addToHand(handCard);
        }
    }

    @Override
    public EntityData getData() {
        return game.getData();
    }

    public Game getGame() {
        return game;
    }

}
