package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.MoveGenerator;
import com.etherblood.a.ai.bots.RandomMover;
import com.etherblood.a.ai.moves.Block;
import com.etherblood.a.ai.moves.Cast;
import com.etherblood.a.ai.moves.DeclareAttack;
import com.etherblood.a.ai.moves.EndPhase;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.Components;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.MinionTemplate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MctsBot {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBot.class);
    private static final boolean VERBOSE = true;
    private static final float EPSILON = 1e-6f;
    private static final float SQRT_2 = (float) Math.sqrt(2);

    private final Game simulationGame;
    private final MoveGenerator moveGenerator = new MoveGenerator();//TODO: optimize by always casting before attacking/blocking to reduce transpositions?
    private final RandomMover randomMover = new RandomMover(moveGenerator);
    private int playerCount;

    public MctsBot(Game simulationGame) {
        this.simulationGame = simulationGame;
    }

    public Move nextMove(Game game) {
        List<Move> moves = moveGenerator.generateMoves(game);
        if (moves.size() == 1) {
            return moves.get(0);
        }
        playerCount = game.getData().list(Components.PLAYER_INDEX).size();
        Node rootNode = createNode();

        int iterations = 10_000;
        for (int i = 0; i < iterations; i++) {
            resetSimulation(game);
            initializeOpponentHandCards();
            iteration(rootNode);
        }
        moves.sort(Comparator.comparingDouble(move -> -rootNode.getChild(move).visits()));
        if (VERBOSE) {
            LOG.info("Move scores after {} iterations:", iterations);
            for (Move move : moves) {
                LOG.info("{}: {}", (int) rootNode.getChild(move).visits(), toMoveString(game, move));
            }
        }
        return moves.get(0);
    }

    private String toMoveString(Game game, Move move) {
        if (move instanceof EndPhase) {
            if (game.getData().list(Components.IN_ATTACK_PHASE).isEmpty()) {
                return "End BlockPhase";
            } else {
                return "End AttackPhase";
            }
        }
        if (move instanceof Cast) {
            Cast cast = (Cast) move;
            return "Cast " + toCardString(game, cast.source) + " -> " + toMinionString(game, cast.target);
        }
        if (move instanceof Block) {
            Block block = (Block) move;
            return "Block " + toMinionString(game, block.source) + " -> " + toMinionString(game, block.target);
        }
        if (move instanceof DeclareAttack) {
            DeclareAttack attack = (DeclareAttack) move;
            return "DeclareAttack " + toMinionString(game, attack.source) + " -> " + toMinionString(game, attack.target);
        }
        return "Unknown Move " + move;
    }

    private String toMinionString(Game game, int minion) {
        if (!game.getData().has(minion, Components.MINION_TEMPLATE)) {
            return "Null";
        }
        int templateId = game.getData().get(minion, Components.MINION_TEMPLATE);
        MinionTemplate template = game.getMinions().apply(templateId);
        return "#" + minion + " " + template.getTemplateName() + " (" + game.getData().getOptional(minion, Components.ATTACK).orElse(0) + ", " + game.getData().getOptional(minion, Components.HEALTH).orElse(0) + ")";
    }

    private String toCardString(Game game, int card) {
        int templateId = game.getData().get(card, Components.CARD_TEMPLATE);
        CardTemplate template = game.getCards().apply(templateId);
        return "#" + card + " " + template.getTemplateName();
    }

    private void initializeOpponentHandCards() {
        // randomize opponents hand cards
        // TODO: use a priori knowlege for better approximation of real hand cards
        // TODO: the simulated opponent also only 'knows' our hand...
        EntityData data = simulationGame.getData();
        int player = simulationGame.getActivePlayer();
        IntList allHandCards = data.list(Components.IN_HAND_ZONE);
        IntList opponentHandCards = new IntList();
        for (int card : allHandCards) {
            if (!data.hasValue(card, Components.OWNED_BY, player)) {
                opponentHandCards.add(card);
            }
        }
        for (int card : opponentHandCards) {
            data.remove(card, Components.IN_HAND_ZONE);
            data.set(card, Components.IN_LIBRARY_ZONE, 1);
        }

        for (int card : opponentHandCards) {
            int owner = data.get(card, Components.OWNED_BY);
            IntList allLibraryCards = data.list(Components.IN_LIBRARY_ZONE);
            IntList ownerLibraryCards = new IntList();
            for (int libraryCard : allLibraryCards) {
                if (data.hasValue(libraryCard, Components.OWNED_BY, owner)) {
                    ownerLibraryCards.add(libraryCard);
                }
            }
            int handCard = ownerLibraryCards.get(simulationGame.getRandom().nextInt(ownerLibraryCards.size()));
            data.set(handCard, Components.IN_HAND_ZONE, 1);
            data.remove(handCard, Components.IN_LIBRARY_ZONE);
        }
    }

    private void resetSimulation(Game sourceGame) {
        EntityUtil.copy(sourceGame.getData(), simulationGame.getData());
    }

    private void iteration(Node rootNode) {
        Deque<Node> nodePath = new LinkedList<>();
        nodePath.add(rootNode);
        Move move = select(nodePath);

        if (move != null) {
            //expand
            Node child = createNode();
            nodePath.getLast().addChild(move, child);
            nodePath.add(child);
            move.apply(simulationGame, simulationGame.getActivePlayer());
        }

        float[] result = rollout();
        for (Node node : nodePath) {
            node.updateScores(result);
        }
    }

    private Move select(Deque<Node> nodePath) {
        Node node = nodePath.getLast();
        Move selectedMove = uctSelect(node);
        while ((node = getChild(node, selectedMove)) != null) {
            nodePath.add(node);
            selectedMove.apply(simulationGame, simulationGame.getActivePlayer());
            if (simulationGame.isGameOver()) {
                return null;
            }
            selectedMove = uctSelect(nodePath.getLast());
        }
        return selectedMove;
    }

    private Move uctSelect(Node node) {
        List<Move> moves = moveGenerator.generateMoves(simulationGame);
        if (moves.size() == 1) {
            return moves.get(0);
        }
        int activePlayer = simulationGame.getActivePlayer();
        int playerIndex = simulationGame.getData().get(activePlayer, Components.PLAYER_INDEX);
        //TODO: skip moves which cant improve if they had same amount of simulations
        List<Move> bestMoves = new ArrayList<>();
        float bestValue = Float.NEGATIVE_INFINITY;
        for (Move move : moves) {
            float uctValue;
            Node child = getChild(node, move);
            if (child == null) {
                uctValue = calcUtc(node.visits(), 0, 0);
            } else {
                uctValue = calcUtc(node.visits(), child.visits(), child.score(playerIndex));
            }
            if (uctValue > bestValue) {
                bestMoves.clear();
                bestValue = uctValue;
            }
            if (uctValue == bestValue) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.size() == 1) {
            return bestMoves.get(0);
        }
        return bestMoves.get(simulationGame.getRandom().nextInt(bestMoves.size()));
    }

    private float calcUtc(float parentScore, float childTotal, float childScore) {
        parentScore += 1;
        childTotal += EPSILON;
        float exploitation = childScore / childTotal;
        float exploration = SQRT_2 * (float) (Math.sqrt(Math.log(parentScore) / childTotal));
        float uctValue = exploitation + exploration;
        return uctValue;
    }

    private Node getChild(Node node, Move move) {
        //TODO: moves should be compared by value equivalence (if card A is identical to card B we can act as if playing either was equivalent)
        // while this is not technically correct it is worth the tradeoff for an improved branching factor
        return node.getChildOrDefault(move, null);
    }

    private float[] rollout() {
        while (!simulationGame.isGameOver()) {
            Move move = randomMover.nextMove(simulationGame);
            move.apply(simulationGame, simulationGame.getActivePlayer());
            simulationGame.isGameOver();
        }
        EntityData data = simulationGame.getData();
        IntList winners = data.list(Components.HAS_WON);
        IntList losers = data.list(Components.HAS_LOST);
        float[] result = new float[playerCount];
        if (winners.isEmpty()) {
            winners = losers;
        }
        for (int winner : winners) {
            int index = data.get(winner, Components.PLAYER_INDEX);
            result[index] += 1f / winners.size();
        }
        return result;
    }

    private Node createNode() {
        return new Node(playerCount);
    }
}
