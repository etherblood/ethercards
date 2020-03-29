package com.etherblood.a.ai;

import com.etherblood.a.ai.movegroups.MoveGroup;
import com.etherblood.a.ai.movegroups.moves.attackphase.DeclareAttack;
import com.etherblood.a.ai.movegroups.moves.attackphase.DeclareAttacker;
import com.etherblood.a.ai.movegroups.moves.attackphase.DeclareTargetedAttackCast;
import com.etherblood.a.ai.movegroups.moves.attackphase.EndAttackCasting;
import com.etherblood.a.ai.movegroups.moves.attackphase.EndAttackPhase;
import com.etherblood.a.ai.movegroups.moves.attackphase.TargetedAttackCast;
import com.etherblood.a.ai.movegroups.moves.attackphase.UntargetedAttackCast;
import com.etherblood.a.ai.movegroups.moves.blockphase.Block;
import com.etherblood.a.ai.movegroups.moves.blockphase.DeclareBlocker;
import com.etherblood.a.ai.movegroups.moves.blockphase.DeclareTargetedBlockCast;
import com.etherblood.a.ai.movegroups.moves.blockphase.EndBlockCasting;
import com.etherblood.a.ai.movegroups.moves.blockphase.EndBlockPhase;
import com.etherblood.a.ai.movegroups.moves.blockphase.TargetedBlockCast;
import com.etherblood.a.ai.movegroups.moves.blockphase.UntargetedBlockCast;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.Stopwatch;
import com.etherblood.a.rules.TimeStats;
import java.util.ArrayList;
import java.util.List;

/**
 * This class splits the actual moves into move groups which are easier to
 * manage by a mcts tree. It reduces the branching factor and avoids some
 * transpositions. Nice side-effect: Generating move groups is much cheaper.
 */
public class MoveGroupBotGame extends BotGameAdapter<MoveGroup, MoveGroupBotGame> {

    private MoveGroup previous;
    private Integer endedCasting;

    public MoveGroupBotGame(Game game) {
        super(game);
    }

    @Override
    public void applyMove(MoveGroup move) {
        if (!move.equals(move)) {
            throw new AssertionError(move);
        }
        try {
            if (move instanceof EndAttackCasting) {
                EndAttackCasting end = (EndAttackCasting) move;
                endedCasting = end.player;
                return;
            }
            if (move instanceof EndBlockCasting) {
                EndBlockCasting end = (EndBlockCasting) move;
                endedCasting = end.player;
                return;
            }
            if (move instanceof EndAttackPhase) {
                EndAttackPhase end = (EndAttackPhase) move;
                endedCasting = null;
                game.endAttackPhase(end.player);
                return;
            }
            if (move instanceof EndBlockPhase) {
                EndBlockPhase end = (EndBlockPhase) move;
                endedCasting = null;
                game.endBlockPhase(end.player);
                return;
            }

            if (move instanceof DeclareTargetedAttackCast) {
                //we only need to set previous
                return;
            }
            if (move instanceof DeclareTargetedBlockCast) {
                //we only need to set previous
                return;
            }

            if (move instanceof TargetedAttackCast) {
                DeclareTargetedAttackCast declared = (DeclareTargetedAttackCast) previous;
                TargetedAttackCast cast = (TargetedAttackCast) move;
                game.cast(cast.player, declared.card, cast.target);
                return;
            }
            if (move instanceof TargetedBlockCast) {
                DeclareTargetedBlockCast declared = (DeclareTargetedBlockCast) previous;
                TargetedBlockCast cast = (TargetedBlockCast) move;
                game.cast(cast.player, declared.card, cast.target);
                return;
            }

            if (move instanceof UntargetedAttackCast) {
                UntargetedAttackCast cast = (UntargetedAttackCast) move;
                game.cast(cast.player, cast.card, null);
                return;
            }

            if (move instanceof UntargetedBlockCast) {
                UntargetedBlockCast cast = (UntargetedBlockCast) move;
                game.cast(cast.player, cast.card, null);
                return;
            }

            if (move instanceof DeclareAttacker) {
                //we only need to set previous
                return;
            }

            if (move instanceof DeclareBlocker) {
                //we only need to set previous
                return;
            }

            if (move instanceof DeclareAttack) {
                DeclareAttacker attacker = (DeclareAttacker) previous;
                DeclareAttack attack = (DeclareAttack) move;
                game.declareAttack(attack.player, attacker.attacker, attack.target);
                return;
            }
            if (move instanceof Block) {
                DeclareBlocker blocker = (DeclareBlocker) previous;
                Block block = (Block) move;
                game.block(block.player, blocker.blocker, block.target);
                return;
            }
            throw new AssertionError(move);
        } finally {
            previous = move;
        }
    }

    @Override
    public List<MoveGroup> generateMoves() {
        try ( Stopwatch stopwatch = TimeStats.get().time("MoveGroupGenerator")) {
            return moveGen();
        }
    }

    private List<MoveGroup> moveGen() {
        EntityData data = game.getData();
        List<MoveGroup> moves = new ArrayList<>();

        if (previous instanceof DeclareAttacker) {
            DeclareAttacker attackerMove = (DeclareAttacker) previous;
            int player = attackerMove.player;
            int attacker = attackerMove.attacker;
            for (int minion : data.list(Components.IN_BATTLE_ZONE)) {
                if (data.hasValue(minion, Components.OWNED_BY, player)) {
                    // technically a valid target, but we prune friendly fire attacks for the AI (for now)
                    continue;
                }
                if (game.canDeclareAttack(player, attacker, minion)) {
                    moves.add(new DeclareAttack(player, minion));
                }
            }
            return moves;
        }

        if (previous instanceof DeclareBlocker) {
            DeclareBlocker blockerMove = (DeclareBlocker) previous;
            int player = blockerMove.player;
            int blocker = blockerMove.blocker;
            for (int minion : data.list(Components.ATTACKS_TARGET)) {
                if (game.canBlock(player, blocker, minion)) {
                    moves.add(new Block(player, minion));
                }
            }
            return moves;
        }

        if (previous instanceof DeclareTargetedAttackCast) {
            DeclareTargetedAttackCast cast = (DeclareTargetedAttackCast) previous;
            for (int target : game.getData().list(Components.IN_BATTLE_ZONE)) {
                moves.add(new TargetedAttackCast(cast.player, target));
            }
            return moves;
        }
        if (previous instanceof DeclareTargetedBlockCast) {
            DeclareTargetedBlockCast cast = (DeclareTargetedBlockCast) previous;
            for (int target : game.getData().list(Components.IN_BATTLE_ZONE)) {
                moves.add(new TargetedBlockCast(cast.player, target));
            }
            return moves;
        }

        for (int player : data.list(Components.IN_ATTACK_PHASE)) {
            if (endedCasting != null && endedCasting == player) {
                for (int minion : data.list(Components.IN_BATTLE_ZONE)) {
                    if (game.canDeclareAttack(player, minion)) {
                        moves.add(new DeclareAttacker(player, minion));
                    }
                }
                moves.add(new EndAttackPhase(player));
            } else {
                endedCasting = null;
                for (int handCard : data.list(Components.IN_HAND_ZONE)) {
                    if (game.canCast(player, handCard, null)) {
                        moves.add(new UntargetedAttackCast(player, handCard));
                    } else if (game.canCast(player, handCard)) {
                        moves.add(new DeclareTargetedAttackCast(player, handCard));
                    }
                }
                moves.add(new EndAttackCasting(player));
            }
        }

        for (int player : data.list(Components.IN_BLOCK_PHASE)) {
            if (endedCasting != null && endedCasting == player) {
                IntList minions = data.list(Components.IN_BATTLE_ZONE);
                for (int minion : minions) {
                    if (game.canBlock(player, minion)) {
                        moves.add(new DeclareBlocker(player, minion));
                    }
                }
                moves.add(new EndBlockPhase(player));
            } else {
                endedCasting = null;
                for (int handCard : data.list(Components.IN_HAND_ZONE)) {
                    if (game.canCast(player, handCard, null)) {
                        moves.add(new UntargetedBlockCast(player, handCard));
                    } else if (game.canCast(player, handCard)) {
                        moves.add(new DeclareTargetedBlockCast(player, handCard));
                    }
                }
                moves.add(new EndBlockCasting(player));
            }
        }
        return moves;
    }

    @Override
    public void copyStateFrom(MoveGroupBotGame source) {
        endedCasting = source.endedCasting;
        previous = source.previous;
        EntityUtil.copy(source.game.getData(), game.getData());
    }

    @Override
    public String toMoveString(MoveGroup move) {
        // TODO
        return move.toString();
    }

}
