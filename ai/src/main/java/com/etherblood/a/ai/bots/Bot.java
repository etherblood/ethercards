package com.etherblood.a.ai.bots;

import com.etherblood.a.rules.moves.Move;

/**
 *
 * @author Philipp
 */
public interface Bot {

    Move findMove(int playerIndex) throws InterruptedException;
}
