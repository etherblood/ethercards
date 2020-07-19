package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
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
