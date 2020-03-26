package com.etherblood.a.ai.bots;

import com.etherblood.a.ai.MoveGenerator;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.rules.Game;
import java.util.List;

public class RandomMover {

    private final MoveGenerator moveGenerator;

    public RandomMover(MoveGenerator moveGenerator) {
        this.moveGenerator = moveGenerator;
    }

    public Move nextMove(Game game) {
        List<Move> moves = moveGenerator.generateMoves(game);
        return moves.get(game.getRandom().nextInt(moves.size()));
    }
}
