package com.etherblood.ethercards.ai.bots;

import com.etherblood.ethercards.rules.moves.Move;

public interface Bot {

    Move findMove(int playerIndex) throws InterruptedException;
}
