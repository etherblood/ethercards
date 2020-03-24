package com.etherblood.a.ai;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.ai.moves.Block;
import com.etherblood.a.ai.moves.Cast;
import com.etherblood.a.ai.moves.DeclareAttack;
import com.etherblood.a.ai.moves.EndPhase;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    public List<Move> generateMoves(Game game) {
        EntityData data = game.getData();
        List<Move> result = new ArrayList<>();
        for (int player : data.list(Components.IN_ATTACK_PHASE)) {
            IntList minions = data.list(Components.IN_BATTLE_ZONE);
            for (int attacker : minions) {
                if (!game.canDeclareAttack(player, attacker)) {
                    continue;
                }
                for (int target : minions) {
                    if (data.hasValue(target, Components.OWNED_BY, player)) {
                        // technically a valid target, but we prune friendly fire attacks for the AI
                        continue;
                    }
                    if (game.canDeclareAttack(player, attacker, target)) {
                        result.add(new DeclareAttack(attacker, target));
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
                addCastMoves(game, handCard, cast, result);
            }
            result.add(new EndPhase());
        }
        for (int player : data.list(Components.IN_BLOCK_PHASE)) {
            IntList minions = data.list(Components.IN_BATTLE_ZONE);
            for (int blocker : minions) {
                if (!game.canBlock(player, blocker)) {
                    continue;
                }
                for (int target : minions) {
                    if (game.canBlock(player, blocker, target)) {
                        result.add(new Block(blocker, target));
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
                addCastMoves(game, handCard, cast, result);
            }
            result.add(new EndPhase());
        }
        if(result.isEmpty()) {
            throw new IllegalStateException();
        }
        // skip generating a surrender move for the AI
        return result;
    }

    private void addCastMoves(Game game, int handCard, CardCast cast, List<Move> result) {
        if (cast.isTargeted()) {
            for (int target : game.getData().list(Components.IN_BATTLE_ZONE)) {
                result.add(new Cast(handCard, target));
            }
        } else {
            result.add(new Cast(handCard, ~0));
        }
    }
}
