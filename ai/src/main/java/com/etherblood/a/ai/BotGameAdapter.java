package com.etherblood.a.ai;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public abstract class BotGameAdapter<T, V extends BotGameAdapter<T, V>> implements BotGame<T, V> {

    protected final Game game;

    public BotGameAdapter(Game game) {
        this.game = game;
    }

    @Override
    public boolean isGameOver() {
        return game.isGameOver();
    }

    @Override
    public int playerCount() {
        return game.getData().list(Components.PLAYER_INDEX).size();
    }

    @Override
    public int activePlayerIndex() {
        return game.getData().get(game.getActivePlayer(), Components.PLAYER_INDEX);
    }

    @Override
    public float[] resultPlayerScores() {
        if (!isGameOver()) {
            throw new IllegalStateException();
        }
        EntityData data = game.getData();
        IntList winners = data.list(Components.HAS_WON);
        IntList losers = data.list(Components.HAS_LOST);
        float[] result = new float[playerCount()];
        if (winners.isEmpty()) {
            winners = losers;
        }
        for (int winner : winners) {
            int index = data.get(winner, Components.PLAYER_INDEX);
            result[index] += 1f / winners.size();
        }
        return result;
    }

    @Override
    public void randomizeHiddenInformation(Random random) {
        EntityData data = game.getData();
        int player = game.getActivePlayer();
        IntList allHandCards = data.list(Components.IN_HAND_ZONE);
        IntList opponentHandCards = new IntList();
        for (int card : allHandCards) {
            if (!data.hasValue(card, Components.OWNED_BY, player)) {
                opponentHandCards.add(card);
            }
        }
        for (int card : opponentHandCards) {
            data.remove(card, Components.IN_HAND_ZONE);
            data.set(card, Components.IN_LIBRARY_ZONE, 1);
        }

        for (int card : opponentHandCards) {
            int owner = data.get(card, Components.OWNED_BY);
            IntList allLibraryCards = data.list(Components.IN_LIBRARY_ZONE);
            IntList ownerLibraryCards = new IntList();
            for (int libraryCard : allLibraryCards) {
                if (data.hasValue(libraryCard, Components.OWNED_BY, owner)) {
                    ownerLibraryCards.add(libraryCard);
                }
            }
            int handCard = ownerLibraryCards.get(random.nextInt(ownerLibraryCards.size()));
            data.set(handCard, Components.IN_HAND_ZONE, 1);
            data.remove(handCard, Components.IN_LIBRARY_ZONE);
        }
    }

    @Override
    public EntityData getData() {
        return game.getData();
    }

    protected String toMinionString(int minion) {
        if (!game.getData().has(minion, Components.MINION_TEMPLATE)) {
            return "Null";
        }
        int templateId = game.getData().get(minion, Components.MINION_TEMPLATE);
        MinionTemplate template = game.getMinions().apply(templateId);
        return "#" + minion + " " + template.getTemplateName() + " (" + game.getData().getOptional(minion, Components.ATTACK).orElse(0) + ", " + game.getData().getOptional(minion, Components.HEALTH).orElse(0) + ")";
    }

    protected String toCardString(int card) {
        int templateId = game.getData().get(card, Components.CARD_TEMPLATE);
        CardTemplate template = game.getCards().apply(templateId);
        return "#" + card + " " + template.getTemplateName();
    }

}
