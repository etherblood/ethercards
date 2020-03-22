package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.MoveGenerator;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.rules.Game;
import java.util.List;

public class RandomMover {

    private final Game game;

    public RandomMover(Game game) {
        this.game = game;
    }

    public Move nextMove() {
        List<Move> moves = new MoveGenerator().generateMoves(game);
        return moves.get(game.getRandom().nextInt(moves.size()));
    }
}
