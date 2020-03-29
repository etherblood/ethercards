package com.etherblood.a.ai;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public interface BotGame<T, V extends BotGame<T, V>> {

    void applyMove(T move);

    List<T> generateMoves();

    boolean isGameOver();

    int playerCount();

    int activePlayerIndex();

    float[] resultPlayerScores();

    void copyStateFrom(V source);

    void randomizeHiddenInformation(Random random);

    String toMoveString(T move);

}
