package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

/**
 *
 * @author Philipp
 */
public interface Move {
    void apply(Game game, int player);
}
