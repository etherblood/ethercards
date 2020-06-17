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
        EntityData data = game.getData();
        List<Move> result = new ArrayList<>();
        for (int player : data.list(core.ACTIVE_PLAYER_PHASE)) {
            int phase = data.get(player, core.ACTIVE_PLAYER_PHASE);
            switch (phase) {
                case PlayerPhase.ATTACK_PHASE: {
                    IntList minions = data.list(core.IN_BATTLE_ZONE);
                    for (int attacker : minions) {
                        if (!game.getMoves().canDeclareAttack(player, attacker)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (pruneFriendlyAttacks) {
                                if (data.hasValue(target, core.OWNED_BY, player)) {
                                    // technically a valid target, but we prune friendly fire attacks for the AI (for now)
                                    continue;
                                }
                            }
                            if (game.getMoves().canDeclareAttack(player, attacker, target)) {
                                result.add(new DeclareAttack(player, attacker, target));
                            }
                        }
                    }
                    IntList handCards = data.list(core.IN_HAND_ZONE);
                    for (int handCard : handCards) {
                        if (!game.getMoves().canCast(player, handCard)) {
                            continue;
                        }
                        CardTemplate template = game.getTemplates().getCard(data.get(handCard, core.CARD_TEMPLATE));
                        CardCast cast = template.getAttackPhaseCast();
                        addCastMoves(game, player, handCard, cast, result);
                    }
                    result.add(new EndAttackPhase(player));
                    break;
                }
                case PlayerPhase.BLOCK_PHASE: {
                    IntList minions = data.list(core.IN_BATTLE_ZONE);
                    for (int blocker : minions) {
                        if (!game.getMoves().canBlock(player, blocker)) {
                            continue;
                        }
                        for (int target : minions) {
                            if (game.getMoves().canBlock(player, blocker, target)) {
                                result.add(new Block(player, blocker, target));
                            }
                        }
                    }
                    IntList handCards = data.list(core.IN_HAND_ZONE);
                    for (int handCard : handCards) {
                        if (!game.getMoves().canCast(player, handCard)) {
                            continue;
                        }
                        CardTemplate template = game.getTemplates().getCard(data.get(handCard, core.CARD_TEMPLATE));
                        CardCast cast = template.getBlockPhaseCast();
                        addCastMoves(game, player, handCard, cast, result);
                    }
                    result.add(new EndBlockPhase(player));
                    break;
                }
                case PlayerPhase.MULLIGAN_PHASE: {
                    for (int card : data.list(core.IN_HAND_ZONE)) {
                        if (game.getMoves().canDeclareMulligan(player, card)) {
                            result.add(new DeclareMulligan(player, card));
                        }
                    }
                    result.add(new EndMulliganPhase(player));
                    break;
                }
                default:
                    throw new AssertionError(phase);
            }
        }
        // skip generating a surrender move for the AI
        return result;
    }

    private void addCastMoves(Game game, int player, int handCard, CardCast cast, List<Move> result) {
        if (cast.isTargeted()) {
            for (int target : TargetUtil.findValidTargets(game.getData(), handCard, cast.getTargets())) {
                result.add(new Cast(player, handCard, target));
            }
        } else {
            result.add(new Cast(player, handCard, ~0));
        }
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
        return "Unknown Move " + move;
    }

    @Override
    public void applyMove(Move move) {
        game.getMoves().move(move);
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
