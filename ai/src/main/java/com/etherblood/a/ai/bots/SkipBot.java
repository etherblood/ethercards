package com.etherblood.ethercards.ai.bots;

import com.etherblood.ethercards.ai.BotGame;
import com.etherblood.ethercards.rules.moves.EndAttackPhase;
import com.etherblood.ethercards.rules.moves.EndBlockPhase;
import com.etherblood.ethercards.rules.moves.EndMulliganPhase;
import com.etherblood.ethercards.rules.moves.Move;
import java.util.List;

public class SkipBot<Game extends BotGame<Move, Game>> implements Bot {

    private final BotGame<Move, Game> game;

    public SkipBot(BotGame<Move, Game> game) {
        this.game = game;
    }

    @Override
    public Move findMove(int playerIndex) {
        List<Move> moves = game.generateMoves(playerIndex);
        for (Move move : moves) {
            if (move instanceof EndAttackPhase || move instanceof EndBlockPhase || move instanceof EndMulliganPhase) {
                return move;
            }
        }
        throw new AssertionError();
    }
}
