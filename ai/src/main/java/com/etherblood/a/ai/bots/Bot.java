package com.etherblood.a.ai.bots;

import com.etherblood.a.rules.moves.Move;

public interface Bot {

    Move findMove(int playerIndex) throws InterruptedException;
}
