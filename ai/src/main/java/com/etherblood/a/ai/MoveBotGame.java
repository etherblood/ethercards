package com.etherblood.a.ai;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.ai.moves.Block;
import com.etherblood.a.ai.moves.Cast;
import com.etherblood.a.ai.moves.DeclareAttack;
import com.etherblood.a.ai.moves.EndAttackPhase;
import com.etherblood.a.ai.moves.EndBlockPhase;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.Stopwatch;
import com.etherblood.a.rules.TimeStats;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.ArrayList;
import java.util.List;

public class MoveBotGame extends BotGameAdapter<Move, MoveBotGame> {

    public MoveBotGame(Game game) {
        super(game);
    }

    @Override
    public List<Move> generateMoves() {
        try ( Stopwatch stopwatch = TimeStats.get().time("MoveGeneratorImpl")) {
            return moveGen();
        }
    }

    private List<Move> moveGen() {
        EntityData data = game.getData();
        List<Move> result = new ArrayList<>();
        for (int player : data.list(Components.ACTIVE_PLAYER_PHASE)) {
            int phase = data.get(player, Components.ACTIVE_PLAYER_PHASE);
            if (phase == PlayerPhase.ATTACK_PHASE) {
                IntList minions = data.list(Components.IN_BATTLE_ZONE);
                for (int attacker : minions) {
                    if (!game.canDeclareAttack(player, attacker)) {
                        continue;
                    }
                    for (int target : minions) {
                        if (data.hasValue(target, Components.OWNED_BY, player)) {
                            // technically a valid target, but we prune friendly fire attacks for the AI (for now)
                            continue;
                        }
                        if (game.canDeclareAttack(player, attacker, target)) {
                            result.add(new DeclareAttack(player, attacker, target));
                        }
                    }
                }
                IntList handCards = data.list(Components.IN_HAND_ZONE);
                for (int handCard : handCards) {
                    if (!game.canCast(player, handCard)) {
                        continue;
                    }
                    CardTemplate template = game.getCards().apply(data.get(handCard, Components.CARD_TEMPLATE));
                    CardCast cast = template.getAttackPhaseCast();
                    addCastMoves(game, player, handCard, cast, result);
                }
                result.add(new EndAttackPhase(player));
            } else {
                IntList minions = data.list(Components.IN_BATTLE_ZONE);
                for (int blocker : minions) {
                    if (!game.canBlock(player, blocker)) {
                        continue;
                    }
                    for (int target : minions) {
                        if (game.canBlock(player, blocker, target)) {
                            result.add(new Block(player, blocker, target));
                        }
                    }
                }
                IntList handCards = data.list(Components.IN_HAND_ZONE);
                for (int handCard : handCards) {
                    if (!game.canCast(player, handCard)) {
                        continue;
                    }
                    CardTemplate template = game.getCards().apply(data.get(handCard, Components.CARD_TEMPLATE));
                    CardCast cast = template.getBlockPhaseCast();
                    addCastMoves(game, player, handCard, cast, result);
                }
                result.add(new EndBlockPhase(player));
            }
        }
        // skip generating a surrender move for the AI
        return result;
    }

    private void addCastMoves(Game game, int player, int handCard, CardCast cast, List<Move> result) {
        if (cast.isTargeted()) {
            for (int target : game.getData().list(Components.IN_BATTLE_ZONE)) {
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
        return "Unknown Move " + move;
    }

    @Override
    public void applyMove(Move move) {
        move.apply(game);
    }

    @Override
    public void copyStateFrom(MoveBotGame source) {
        if (game.getCards() != source.game.getCards()) {
            throw new IllegalArgumentException();
        }
        if (game.getMinions() != source.game.getMinions()) {
            throw new IllegalArgumentException();
        }
        EntityUtil.copy(source.game.getData(), game.getData());
    }

}
