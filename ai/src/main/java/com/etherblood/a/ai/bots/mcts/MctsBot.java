package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.rules.Stopwatch;
import com.etherblood.a.rules.TimeStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MctsBot<Move, Game extends BotGame<Move, Game>> {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBot.class);
    private static final float EPSILON = 1e-6f;
    private final boolean verbose;
    private final float uctConstant;
    private final float firstPlayUrgency;
    private final Function<Game, float[]> evaluation;

    private final Game simulationGame;
    private final Random random;
    private final int strength;
    private final float raveMultiplier;

    public MctsBot(Game simulationGame, MctsBotSettings settings) {
        this.simulationGame = simulationGame;
        this.random = settings.random;
        this.strength = settings.strength;
        this.verbose = settings.verbose;
        this.uctConstant = settings.uctConstant;
        this.firstPlayUrgency = settings.firstPlayUrgency;
        this.evaluation = settings.evaluation;
        this.raveMultiplier = settings.raveMultiplier;
    }

    public void playTurn(Game game) {
        int iterations = 0;
        long start = System.nanoTime();
        try ( Stopwatch stopwatch = TimeStats.get().time("MctsBot")) {
            int player = game.activePlayerIndex();
            Node rootNode = createNode();
            Map<Move, RaveScore> raveScores = new HashMap<>();
            RaveScore defaultScore = new RaveScore(simulationGame.playerCount());
            defaultScore.updateScores(1f / simulationGame.playerCount());
            raveScores.put(null, defaultScore);
            do {
                List<Move> moves = new ArrayList<>(game.generateMoves());
                if (moves.size() > 1) {
                    while (rootNode.visits() < strength) {
                        try ( Stopwatch resetStopwatch = TimeStats.get().time("MctsBot.resetSimulation()")) {
                            simulationGame.copyStateFrom(game);
                        }
                        try ( Stopwatch randomizeStopwatch = TimeStats.get().time("MctsBot.initializeOpponentHandCards()")) {
                            // randomize opponents hand cards
                            // TODO: use a priori knowlege for better approximation of real hand cards
                            // TODO: the simulated opponent also only 'knows' our hand...
                            simulationGame.randomizeHiddenInformation(random);
                        }
                        try ( Stopwatch iterateStopwatch = TimeStats.get().time("MctsBot.iteration()")) {
                            iteration(rootNode, raveScores);
                        }
                        iterations++;
                    }

                    Node node = rootNode;
                    moves.sort(Comparator.comparingDouble(move -> -node.getChild(move).visits()));
                    if (verbose) {
                        LOG.info("Move scores:");
                        for (Move move : moves) {
                            LOG.info("{}: {}", node.getChild(move).visits(), game.toMoveString(move));
                        }
                        LOG.info("Expected winrate: {}%", (int) (100 * rootNode.score(game.activePlayerIndex()) / rootNode.visits()));
                    }
                }
                Move move = moves.get(0);
                game.applyMove(move);
                rootNode = rootNode.getChildOrDefault(move, createNode());
            } while (!game.isGameOver() && player == game.activePlayerIndex());
        }
        if (verbose) {
            LOG.info("Total of {} iterations for turn in {}.", iterations, TimeStats.humanReadableNanos(System.nanoTime() - start));
        }
    }

    private void iteration(Node rootNode, Map<Move, RaveScore> raveScores) {
        Deque<Node> nodePath = new LinkedList<>();
        Deque<Move> movePath = new LinkedList<>();
        nodePath.add(rootNode);
        Move selectedMove = select(nodePath, movePath, raveScores);

        if (selectedMove != null) {
            Node child = createNode();
            nodePath.getLast().addChild(selectedMove, child);
            nodePath.add(child);
            movePath.add(selectedMove);
            simulationGame.applyMove(selectedMove);
        }

        try ( Stopwatch resetStopwatch = TimeStats.get().time("MctsBot.rollout()")) {
            float[] result = evaluation.apply(simulationGame);
            for (Node node : nodePath) {
                node.updateScores(result);
            }
            for (Move move : movePath) {
                raveScores.computeIfAbsent(move, x -> new RaveScore(simulationGame.playerCount())).updateScores(result);
            }
            float[] avgWeights = Arrays.copyOf(result, result.length);
            for (int i = 0; i < avgWeights.length; i++) {
                avgWeights[i] *= movePath.size();
            }
            raveScores.get(null).updateScores(avgWeights);
        }
    }

    private Move select(Deque<Node> nodePath, Deque<Move> movePath, Map<Move, RaveScore> raveScores) {
        Node node = nodePath.getLast();
        Move selectedMove = uctSelect(node, raveScores);
        while ((node = getChild(node, selectedMove)) != null) {
            nodePath.add(node);
            movePath.add(selectedMove);
            simulationGame.applyMove(selectedMove);
            if (simulationGame.isGameOver()) {
                return null;
            }
            selectedMove = uctSelect(nodePath.getLast(), raveScores);
        }
        return selectedMove;
    }

    private Move uctSelect(Node node, Map<Move, RaveScore> raveScores) {
        List<Move> moves = simulationGame.generateMoves();
        if (moves.size() == 1) {
            return moves.get(0);
        }
        int playerIndex = simulationGame.activePlayerIndex();
        //TODO: skip moves which cant improve if they had same amount of simulations
        List<Move> bestMoves = new ArrayList<>();
        float bestValue = Float.NEGATIVE_INFINITY;
        for (Move move : moves) {
            float score;
            Node child = getChild(node, move);
            if (child == null) {
                score = firstPlayUrgency;
            } else {
                score = calcUtc(node.visits(), child.visits(), child.score(playerIndex));
            }

            RaveScore raveScore = raveScores.get(move);
            if (raveScore == null) {
                raveScore = raveScores.get(null);
            }
            float raveValue = raveScore.getScore(playerIndex) / (node.visits() + 1);
            score += raveMultiplier * raveValue;

            if (score > bestValue) {
                bestMoves.clear();
                bestValue = score;
            }
            if (score == bestValue) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.size() == 1) {
            return bestMoves.get(0);
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private float calcUtc(float parentTotal, float childTotal, float childScore) {
        float exploitation = childScore / (childTotal + EPSILON);
        float exploration = (float) (Math.sqrt(uctConstant * Math.log(parentTotal + 1) / (childTotal + EPSILON)));
        float uctValue = exploitation + exploration;
        return uctValue;
    }

    private Node getChild(Node node, Move move) {
        return node.getChildOrDefault(move, null);
    }

    private Node<Move> createNode() {
        return new Node<>(simulationGame.playerCount());
    }
}
