package com.etherblood.a.ai;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.Random;

/**
 *
 * @author Philipp
 */
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
    public int activePlayerIndex() {
        for (int player : game.getData().list(core.ACTIVE_PLAYER_PHASE)) {
            return game.getData().get(player, core.PLAYER_INDEX);
        }
        throw new AssertionError();
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
            if (data.get(player, core.PLAYER_RESULT) == PlayerResult.VICTORY) {
                winners.add(player);
            } else {
                losers.add(player);
            }
        }
        float[] result = new float[playerCount()];
        if (winners.isEmpty()) {
            winners = losers;
        }
        for (int winner : winners) {
            int index = data.get(winner, core.PLAYER_INDEX);
            result[index] += 1f / winners.size();
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
        IntList allHandCards = data.list(core.IN_HAND_ZONE);
        IntList opponentHandCards = new IntList();
        for (int card : allHandCards) {
            if (!data.hasValue(card, core.OWNED_BY, self)) {
                opponentHandCards.add(card);
            }
        }
        for (int card : opponentHandCards) {
            data.remove(card, core.IN_HAND_ZONE);
            data.remove(card, core.MULLIGAN);
            data.set(card, core.IN_LIBRARY_ZONE, 1);
        }

        for (int card : opponentHandCards) {
            int owner = data.get(card, core.OWNED_BY);
            IntList allLibraryCards = data.list(core.IN_LIBRARY_ZONE);
            IntList ownerLibraryCards = new IntList();
            for (int libraryCard : allLibraryCards) {
                if (data.hasValue(libraryCard, core.OWNED_BY, owner)) {
                    ownerLibraryCards.add(libraryCard);
                }
            }
            int handCard = ownerLibraryCards.get(random.nextInt(ownerLibraryCards.size()));
            data.set(handCard, core.IN_HAND_ZONE, 1);
            data.remove(handCard, core.IN_LIBRARY_ZONE);
        }
    }

    @Override
    public EntityData getData() {
        return game.getData();
    }

    protected String toMinionString(int minion) {
        if (!game.getData().has(minion, core.MINION_TEMPLATE)) {
            return "Null";
        }
        int templateId = game.getData().get(minion, core.MINION_TEMPLATE);
        MinionTemplate template = game.getTemplates().getMinion(templateId);
        return "#" + minion + " " + template.getTemplateName() + " (" + game.getData().getOptional(minion, core.ATTACK).orElse(0) + ", " + game.getData().getOptional(minion, core.HEALTH).orElse(0) + ")";
    }

    protected String toCardString(int card) {
        int templateId = game.getData().get(card, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(templateId);
        return "#" + card + " " + template.getTemplateName();
    }

    public Game getGame() {
        return game;
    }

}
