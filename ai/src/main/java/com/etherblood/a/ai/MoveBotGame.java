package com.etherblood.a.ai;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.effects.targeting.TargetUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoveBotGame extends BotGameAdapter<Move, MoveBotGame> {

    public boolean pruneFriendlyAttacks = true;

    public MoveBotGame(Game game) {
        super(game);
    }

    @Override
    public List<Move> generateMoves() {
        return game.getMoves().generate(pruneFriendlyAttacks, true);
    }

    @Override
    public String toMoveString(Move move) {
        if (move instanceof EndBlockPhase) {
            return "End BlockPhase";
        }
        if (move instanceof EndAttackPhase) {
            return "End AttackPhase";
        }
        if (move instanceof EndMulliganPhase) {
            return "End MulliganPhase";
        }
        if (move instanceof DeclareMulligan) {
            DeclareMulligan mulligan = (DeclareMulligan) move;
            return "DeclareMulligan " + toCardString(mulligan.card);
        }
        if (move instanceof Cast) {
            Cast cast = (Cast) move;
            return "Cast " + toCardString(cast.source) + " -> " + toMinionString(cast.target);
        }
        if (move instanceof Block) {
            Block block = (Block) move;
            return "Block " + toMinionString(block.source) + " -> " + toMinionString(block.target);
        }
        if (move instanceof DeclareAttack) {
            DeclareAttack attack = (DeclareAttack) move;
            return "DeclareAttack " + toMinionString(attack.source) + " -> " + toMinionString(attack.target);
        }
        if (move instanceof Start) {
            return "Start";
        }
        return String.valueOf(move);
    }

    @Override
    public void applyMove(Move move) {
        game.getMoves().apply(move);
    }

    @Override
    public void copyStateFrom(MoveBotGame source) {
        if (game.getTemplates() != source.game.getTemplates()) {
            // equals() would be slow, require reference identity for performance
            throw new IllegalArgumentException();
        }
        EntityUtil.copy(source.game.getData(), game.getData());
    }

    @Override
    public List<Move> getMoveHistory() {
        return game.getMoves().getHistory().stream().map(x -> x.move).collect(Collectors.toList());
    }

}
