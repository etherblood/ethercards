package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.MoveGenerator;
import com.etherblood.a.ai.bots.RandomMover;
import com.etherblood.a.ai.moves.Move;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class MctsBot {

    private final static float EPSILON = 1e-6f;
    private final static float SQRT_2 = (float) Math.sqrt(2);

    private Node rootNode;
    private final Game simulationGame;
    private final MoveGenerator moveGenerator = new MoveGenerator();//TODO: optimize by always casting before attacking/blocking
    private final RandomMover randomMover = new RandomMover();

    public MctsBot(Game simulationGame) {
        if (simulationGame.getPlayers().length != 2) {
            throw new UnsupportedOperationException();
        }
        this.simulationGame = simulationGame;
    }

    public Move nextMove(Game game) {
        rootNode = new Node();
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            resetSimulation(game);
            //TODO: hide opponent hand cards
            iteration();
        }
        List<Move> moves = moveGenerator.generateMoves(game);
        Move bestMove = moves.get(0);
        float bestScore = rootNode.childs.getOrDefault(bestMove, new Node()).visits;
        for (int i = 1; i < moves.size(); i++) {
            Move move = moves.get(i);
            float score = rootNode.childs.getOrDefault(move, new Node()).visits;
            if (score > bestScore) {
                bestMove = move;
                bestScore = score;
            }
        }
        return bestMove;
    }

    private void resetSimulation(Game sourceGame) {
        EntityUtil.copy(sourceGame.getData(), simulationGame.getData());
    }

    private void iteration() {
        Deque<Node> nodePath = new LinkedList<>();
        nodePath.add(rootNode);
        Move move = select(nodePath);

        if (move != null) {
            //expand
            Node child = new Node();
            nodePath.add(child);
            move.apply(simulationGame, simulationGame.getActivePlayer());
        }

        float result = rollout();
        for (Node node : nodePath) {
            node.visits++;
            node.score += result;
        }
    }

    private Move select(Deque<Node> nodePath) {
        Node node = nodePath.getLast();
        Move selectedMove = uctSelect(node);
        while ((node = getChild(node, selectedMove)) != null) {
            nodePath.add(node);
            selectedMove.apply(simulationGame, simulationGame.getActivePlayer());
            selectedMove = uctSelect(nodePath.getLast());
            if (simulationGame.isGameOver()) {
                return null;
            }
        }
        return selectedMove;
    }

    private Move uctSelect(Node node) {
        boolean player0active = simulationGame.getActivePlayer() == simulationGame.getPlayers()[0];
        List<Move> moves = moveGenerator.generateMoves(simulationGame);
        //TODO: skip moves which cant improve if they had same amount of simulations
        Move best = null;
        float bestValue = Float.NEGATIVE_INFINITY;
        for (Move move : moves) {
            float uctValue;
            Node child = getChild(node, move);
            if (child == null) {
                //TODO: better value
                uctValue = 0.5f;
            } else {
                uctValue = calcUtc(node.visits, child.visits, player0active ? child.score : child.visits - child.score);
            }
            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = move;
            }
            //TODO: better tie breaker
        }
        return best;
    }

    private float calcUtc(float parentScore, float childTotal, float childScore) {
        childTotal += EPSILON;
        float exploitation = childScore / childTotal;
        float exploration = SQRT_2 * (float) (Math.sqrt(Math.log(parentScore) / childTotal));
        float uctValue = exploitation + exploration;
        return uctValue;
    }

    private Node getChild(Node node, Move move) {
        //TODO: moves should be compared by value equivalence (if card A is identical to card B we can act as if playing either was equivalent)
        // while this is not technically correct it is worth the tradeoff for an improved branching factor
        return node.childs.get(move);
    }

    private float rollout() {
        while (!simulationGame.isGameOver()) {
            randomMover.nextMove(simulationGame).apply(simulationGame, simulationGame.getActivePlayer());
        }
        int[] players = simulationGame.getPlayers();
        return simulationGame.hasPlayerWon(players[0]) ? 1 : simulationGame.hasPlayerWon(players[1]) ? 0 : 1f / players.length;
    }
}
